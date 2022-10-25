package me.yapoo.oauth.router

import arrow.core.continuations.either
import arrow.core.merge
import me.yapoo.oauth.handler.authentication.AuthenticationHandler
import me.yapoo.oauth.handler.authorization.AuthorizationHandler
import me.yapoo.oauth.handler.client.ClientRegistrationHandler
import me.yapoo.oauth.handler.metadata.MetadataHandler
import me.yapoo.oauth.handler.revoke.TokenRevocationHandler
import me.yapoo.oauth.handler.token.TokenAuthorizationCodeHandler
import me.yapoo.oauth.handler.token.TokenErrorResponse
import me.yapoo.oauth.handler.token.TokenRefreshTokenHandler
import me.yapoo.oauth.mixin.spring.getSingle
import me.yapoo.oauth.router.authentication.client.ClientAuthenticator
import me.yapoo.oauth.router.authentication.token.BearerTokenAuthenticator
import me.yapoo.oauth.router.error.handleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class Router(
    private val authorizationHandler: AuthorizationHandler,
    private val authenticationHandler: AuthenticationHandler,
    private val tokenRefreshTokenHandler: TokenRefreshTokenHandler,
    private val tokenAuthorizationCodeHandler: TokenAuthorizationCodeHandler,
    private val clientAuthenticator: ClientAuthenticator,
    private val clientRegistrationHandler: ClientRegistrationHandler,
    private val bearerTokenAuthenticator: BearerTokenAuthenticator,
    private val tokenRevocationHandler: TokenRevocationHandler,
    private val metadataHandler: MetadataHandler,
) {

    @Bean
    fun routes() = coRouter {

        POST("/client") {
            clientRegistrationHandler.handle(it).merge()
        }
        GET("/authorization") {
            authorizationHandler.handle(it).merge()
        }
        POST("/authentication") {
            authenticationHandler.handle(it).merge()
        }
        POST("/token") {
            val client = clientAuthenticator.doAuthentication(it)

            when (it.awaitFormData().getSingle("grant_type")) {
                "authorization_code" -> tokenAuthorizationCodeHandler.handle(it, client).merge()
                "refresh_token" -> tokenRefreshTokenHandler.handle(it, client).merge()
                else -> ServerResponse.badRequest().bodyValueAndAwait(
                    TokenErrorResponse(
                        TokenErrorResponse.ErrorCode.UnsupportedGrantType,
                        "invalid grant_type"
                    )
                )
            }
        }
        POST("/revoke") {
            val client = clientAuthenticator.doAuthentication(it)

            tokenRevocationHandler.handle(it, client).merge()
        }
        GET("/hello") {
            either {
                bearerTokenAuthenticator.doAuthentication(it, listOf("hello")).bind()

                ServerResponse.ok()
                    .bodyValueAndAwait(
                        mapOf(
                            "language" to "日本語",
                            "value" to "こんにちは"
                        )
                    )
            }.merge()
        }

        GET("/world") {
            either {
                bearerTokenAuthenticator.doAuthentication(it, listOf("world")).bind()

                ServerResponse.ok()
                    .bodyValueAndAwait(
                        mapOf(
                            "language" to "日本語",
                            "value" to "世界"
                        )
                    )
            }.merge()
        }

        ".well-known".nest {
            GET("oauth-authorization-server") {
                metadataHandler.handle()
            }
        }

        handleException(logger)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
