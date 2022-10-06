package me.yapoo.oauth.router

import arrow.core.merge
import me.yapoo.oauth.handler.authentication.AuthenticationHandler
import me.yapoo.oauth.handler.authorization.AuthorizationHandler
import me.yapoo.oauth.handler.token.TokenAuthorizationCodeHandler
import me.yapoo.oauth.handler.token.TokenErrorResponse
import me.yapoo.oauth.handler.token.TokenRefreshTokenHandler
import me.yapoo.oauth.mixin.spring.getSingle
import me.yapoo.oauth.router.authentication.ClientAuthenticator
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
) {

    @Bean
    fun routes() = coRouter {
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

        handleException(logger)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
