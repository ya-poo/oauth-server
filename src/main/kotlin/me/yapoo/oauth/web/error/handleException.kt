package me.yapoo.oauth.web.error

import me.yapoo.oauth.log.error
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

fun CoRouterFunctionDsl.handleException(
    logger: Logger
) {
    onError<Exception> { ex, _ ->
        logger.error(ex) { "Handled Unexpected Exception." }
        ServerResponse
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyValueAndAwait(ErrorResponse("internal_server_error", "Unexpected error."))
    }
}
