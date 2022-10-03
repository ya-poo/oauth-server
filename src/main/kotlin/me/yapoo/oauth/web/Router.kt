package me.yapoo.oauth.web

import arrow.core.merge
import me.yapoo.oauth.handler.authentication.AuthenticationHandler
import me.yapoo.oauth.handler.authorization.AuthorizationHandler
import me.yapoo.oauth.web.error.handleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.coRouter

@Component
class Router(
    private val authorizationHandler: AuthorizationHandler,
    private val authenticationHandler: AuthenticationHandler,
) {

    @Bean
    fun routes() = coRouter {
        GET("/authorization") {
            authorizationHandler.handle(it).merge()
        }
        POST("/authentication") {
            authenticationHandler.handle(it).merge()
        }
        handleException(logger)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
