package me.yapoo.oauth.router.authentication.token

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.Authorization
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.infrastructure.time.DateTimeFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait

@Component
class BearerTokenAuthenticator(
    private val accessTokenRepository: AccessTokenRepository,
    private val authorizationRepository: AuthorizationRepository,
    private val dateTimeFactory: DateTimeFactory,
) {

    // Bearer トークン認証 (RFC 6750 - 2.1, 3)
    // Authorization ヘッダによるもののみサポートする。
    suspend fun doAuthentication(
        request: ServerRequest,
        requiredScopes: List<String>,
    ): Either<ServerResponse, Authorization> {
        return either {
            val authorizationHeaderValue = request.headers().header("Authorization")
                .singleOrNull()
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Bearer realm=\"oauth-server\"")
                        .buildAndAwait()
                }.bind()

            coEnsure(authorizationHeaderValue.startsWith("Bearer ")) {
                ServerResponse.badRequest()
                    .header(
                        "WWW-Authenticate",
                        "Bearer realm=\"oauth-server\"",
                        "error=\"${BearerTokenErrorCode.InvalidRequest.value}\"",
                    )
                    .buildAndAwait()
            }

            val token = accessTokenRepository.findByToken(authorizationHeaderValue.substring(7))
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .header(
                            "WWW-Authenticate",
                            "Bearer realm=\"oauth-server\"",
                            "error=\"${BearerTokenErrorCode.InvalidToken.value}\"",
                        )
                        .buildAndAwait()
                }.bind()

            val now = dateTimeFactory.now()
            coEnsure(!token.expired(now)) {
                ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .header(
                        "WWW-Authenticate",
                        "Bearer realm=\"oauth-server\"",
                        "error=\"${BearerTokenErrorCode.InvalidToken.value}\"",
                        "error_description=\"The access token expired\""
                    )
                    .buildAndAwait()
            }

            val authorization = authorizationRepository.findById(token.authorizationId)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .header(
                            "WWW-Authenticate",
                            "Bearer realm=\"oauth-server\"",
                            "error=\"${BearerTokenErrorCode.InvalidToken.value}\"",
                        )
                        .buildAndAwait()
                }.bind()

            coEnsure(authorization.scopes.containsAll(requiredScopes)) {
                ServerResponse.status(HttpStatus.FORBIDDEN)
                    .header(
                        "WWW-Authenticate",
                        "Bearer realm=\"oauth-server\"",
                        "error=\"${BearerTokenErrorCode.InsufficientScope.value}\"",
                        "scope=${requiredScopes.joinToString(" ")}"
                    )
                    .buildAndAwait()
            }

            authorization
        }
    }
}
