package me.yapoo.oauth.handler.token

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.AuthorizationCodeRepository
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.authorization.RefreshToken
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.time.DateTimeFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class TokenHandler(
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val authorizationRepository: AuthorizationRepository,
    private val authorizationSessionRepository: AuthorizationSessionRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clientRepository: ClientRepository,
    private val dateTimeFactory: DateTimeFactory,
    private val secureStringFactory: SecureStringFactory,
) {

    // トークンエンドポイント (RFC 6749 - 3.2)
    // TODO : refresh_token
    suspend fun handle(
        request: ServerRequest
    ): Either<ServerResponse, ServerResponse> {
        return either {
            // RFC 6749 - 3.2, 4.1.3
            // TODO : クライアント認証
            val body = request.awaitFormData()
            val grantType = body.getSingle("grant_type")
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidRequest,
                            "grant_type must be specified"
                        )
                    )
                }.bind()
            when (grantType) {
                "authorization_code" -> handleAuthorizationCode(body).bind()
                else -> ServerResponse.badRequest().bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.UnsupportedGrantType,
                        "invalid grant_type"
                    )
                )
            }
        }
    }

    private suspend fun handleAuthorizationCode(
        body: MultiValueMap<String, String>
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val code = body.getSingle("code")
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidRequest,
                            "code must be specified"
                        )
                    )
                }.bind()
            val authorizationCode = authorizationCodeRepository.findByCode(code)
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "invalid code value"
                        )
                    )
                }.bind()
            val authorizationSession = authorizationSessionRepository.findById(authorizationCode.authorizationSessionId)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InternalServerError,
                            "please retry later"
                        )
                    )
                }.bind()
            val client = clientRepository.findById(authorizationSession.clientId)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.NOT_FOUND).bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.ClientNotFound,
                            "client which is associated with authorization code was not found"
                        )
                    )
                }.bind()
            val clientId = body.getSingle("client_id")
            when (client.type) {
                Client.Type.Confidential -> {
                    // TODO("認証情報と client の値の比較")
                }
                Client.Type.Public -> {
                    coEnsure(client.id.value == clientId) {
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValueAndAwait(
                            TokenErrorResponse(
                                TokenErrorResponse.ErrorCode.InvalidClient,
                                "requested client_id is not associated with authorization code"
                            )
                        )
                    }
                }
            }
            val redirectUri = body.getSingle("redirect_uri")
            coEnsure(
                !authorizationSession.redirectUriSpecified ||
                        authorizationSession.redirectUri == redirectUri
            ) {
                ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.InvalidGrant,
                        "request redirect_uri is invalid"
                    )
                )
            }

            val now = dateTimeFactory.now()
            coEnsure(!authorizationCode.isExpired(now)) {
                ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.InvalidGrant,
                        "expired authorization code"
                    )
                )
            }

            val authorization = authorizationRepository.findById(authorizationCode.authorizationId)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "expired authorization code"
                        )
                    )
                }.bind()

            val accessToken = AccessToken.new(
                secureStringFactory,
                authorizationCode.authorizationId,
                now
            )
            accessTokenRepository.save(accessToken)
            val refreshToken = RefreshToken.new(
                secureStringFactory,
                authorizationCode.authorizationId,
                now
            )
            refreshTokenRepository.save(refreshToken)

            // RFC 6749 - 5.1
            ServerResponse.ok()
                .headers {
                    it.cacheControl = "no-store"
                    it.pragma = "no-cache"
                }
                .bodyValueAndAwait(
                    TokenResponse(
                        accessToken = accessToken.value,
                        expiresIn = accessToken.expiresIn.seconds.toInt(),
                        refreshToken = refreshToken.value,
                        scope = authorization.scopes
                    )
                )
        }
    }

    private fun MultiValueMap<String, String>.getSingle(
        key: String
    ): String? {
        return this[key]?.singleOrNull()
    }
}
