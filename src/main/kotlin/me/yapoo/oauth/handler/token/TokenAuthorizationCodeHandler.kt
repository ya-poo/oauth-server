package me.yapoo.oauth.handler.token

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.config.RsaKeyPair
import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.Authorization
import me.yapoo.oauth.domain.authorization.AuthorizationCodeRepository
import me.yapoo.oauth.domain.authorization.AuthorizationId
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.authorization.RefreshToken
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.random.UuidFactory
import me.yapoo.oauth.infrastructure.time.SystemClock
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.coEnsureNotNull
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class TokenAuthorizationCodeHandler(
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val authorizationRepository: AuthorizationRepository,
    private val authorizationSessionRepository: AuthorizationSessionRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clientRepository: ClientRepository,
    private val systemClock: SystemClock,
    private val secureStringFactory: SecureStringFactory,
    private val rsaKeyPair: RsaKeyPair,
    private val uuidFactory: UuidFactory,
) {

    // トークンエンドポイント (RFC 6749 - 3.2)
    // 認可コードフローにおけるアクセストークン発行 (RFC 6749 - 4.1.3, 4.1.4, 5.1, 5.2)
    // PKCE 検証 (RFC 7636)
    suspend fun handle(
        request: ServerRequest,
        client: Client?
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitFormData()
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
                    ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "expired authorization code"
                        )
                    )
                }.bind()
            val codeVerifier = body.getSingle("code_verifier")
            if (authorizationSession.proofKey != null) {
                coEnsure(codeVerifier != null && authorizationSession.proofKey.accepts(codeVerifier)) {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "invalid code_verifier value"
                        )
                    )
                }
            }

            val authorizationCodeClient = clientRepository.findById(authorizationSession.clientId)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.NOT_FOUND).bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.ClientNotFound,
                            "client which is associated with authorization code was not found"
                        )
                    )
                }.bind()
            val clientId = body.getSingle("client_id")
            when (authorizationCodeClient.type) {
                Client.Type.Confidential -> {
                    coEnsureNotNull(client) {
                        ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .headers {
                                it.set("WWW-Authenticate", "Basic realm=\"oauth-server\"")
                            }
                            .bodyValueAndAwait(
                                TokenErrorResponse(
                                    TokenErrorResponse.ErrorCode.InvalidClient,
                                    "failed client authentication"
                                )
                            )
                    }
                    coEnsure(
                        client.id == authorizationCodeClient.id &&
                            // RFC ではリクエストボディの `client_id` のチェックは特に求められてはいない
                            (clientId == null || clientId == client.id.value)
                    ) {
                        ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                            TokenErrorResponse(
                                TokenErrorResponse.ErrorCode.InvalidGrant,
                                "invalid authorization code"
                            )
                        )
                    }
                }
                Client.Type.Public -> {
                    coEnsure(authorizationCodeClient.id.value == clientId) {
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

            val now = systemClock.now()
            coEnsure(now < authorizationCode.expiresAt) {
                ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.InvalidGrant,
                        "expired authorization code"
                    )
                )
            }

            val authorizationId = AuthorizationId.new(uuidFactory)
            val authorization = Authorization.new(
                id = authorizationId,
                userSubject = authorizationCode.userSubject,
                clientId = authorizationSession.clientId,
                scopes = authorizationSession.scopes,
            )
            authorizationRepository.add(authorization)

            val idToken = if (authorizationSession.openId.required) {
                // 認可コード発行時刻を `auth_time` とする。
                IdToken(
                    sub = authorization.userSubject,
                    clientId = authorization.clientId,
                    now = now,
                    authTime = authorizationCode.issuedAt,
                    nonce = authorizationSession.openId.nonce,
                    rsaPublicKey = rsaKeyPair.public,
                    rsaPrivateKey = rsaKeyPair.private,
                )
            } else null

            val accessToken = AccessToken.new(
                secureStringFactory,
                authorization.id,
                now
            )
            accessTokenRepository.add(accessToken)
            val refreshToken = RefreshToken.new(
                secureStringFactory,
                authorization.id,
                now
            )
            refreshTokenRepository.add(refreshToken)

            // RFC 6749 - 5.1
            ServerResponse.ok()
                .headers {
                    it.cacheControl = "no-store"
                    it.pragma = "no-cache"
                }
                .bodyValueAndAwait(
                    TokenAuthorizationCodeResponse(
                        accessToken = accessToken.value,
                        expiresIn = AccessToken.expiresIn.seconds.toInt(),
                        refreshToken = refreshToken.value,
                        scope = authorization.scopes,
                        idToken = idToken?.value
                    )
                )
        }
    }
}
