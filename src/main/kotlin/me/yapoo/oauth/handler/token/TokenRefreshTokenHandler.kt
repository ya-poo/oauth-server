package me.yapoo.oauth.handler.token

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.time.DateTimeFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.rightIfNotEmpty
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class TokenRefreshTokenHandler(
    private val authorizationRepository: AuthorizationRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val dateTimeFactory: DateTimeFactory,
    private val secureStringFactory: SecureStringFactory,
) {

    // トークンエンドポイント (RFC 6749 - 3.2)
    // RFC 6749 - 5.1, 5.2, 6
    // リフレッシュトークンによるアクセストークン更新
    suspend fun handle(
        request: ServerRequest
    ): Either<ServerResponse, ServerResponse> {
        return either {
            // TODO : クライアント認証
            val body = request.awaitFormData()
            val requestRefreshToken = body.getSingle("refresh_token")
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidRequest,
                            "refresh_token must be specified"
                        )
                    )
                }.bind()

            val refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "invalid refresh_token"
                        )
                    )
                }.bind()
            val now = dateTimeFactory.now()
            coEnsure(!refreshToken.expired(now)) {
                ServerResponse.badRequest().bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.InvalidGrant,
                        "refresh_token expired"
                    )
                )
            }

            val authorization = authorizationRepository.findById(refreshToken.authorizationId)
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidGrant,
                            "invalid refresh_token"
                        )
                    )
                }.bind()
            val scopes = (body.getSingle("scope")?.split(" ")
                ?: authorization.scopes)
                .rightIfNotEmpty {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenErrorResponse(
                            TokenErrorResponse.ErrorCode.InvalidScope,
                            "invalid scope"
                        )
                    )
                }.bind()
            coEnsure(authorization.scopes.containsAll(scopes)) {
                ServerResponse.badRequest().bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.InvalidScope,
                        "invalid scope"
                    )
                )
            }
            authorizationRepository.save(
                authorization.copy(scopes = scopes)
            )
            val nextRefreshToken = refreshToken.next(secureStringFactory, now)
            refreshTokenRepository.save(nextRefreshToken)

            val accessToken = AccessToken.new(secureStringFactory, authorization.id, now)
            accessTokenRepository.save(accessToken)

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
                        scope = scopes
                    )
                )
        }
    }
}
