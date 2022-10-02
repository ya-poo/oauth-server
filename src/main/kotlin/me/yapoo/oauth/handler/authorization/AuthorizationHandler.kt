package me.yapoo.oauth.handler.authorization

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.State
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.log.info
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.coEnsureNotNull
import me.yapoo.oauth.web.error.ErrorCode
import me.yapoo.oauth.web.error.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI

@Service
class AuthorizationHandler(
    private val clientRepository: ClientRepository
) {

    // 認可エンドポイント (RFC 6749 - 3.1)
    // 現在は認可コードフロー (RFC 6749 - 4.1) のみ対応
    suspend fun handle(
        request: ServerRequest
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val clientId = request.queryParamOrNull("client_id")
                ?.let(::ClientId)
                .rightIfNotNull {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            ErrorResponse(ErrorCode.BAD_REQUEST.value, "`client_id` must be specified.")
                        )
                }
                .bind()
            val client = clientRepository.findById(clientId)
                .rightIfNotNull {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            ErrorResponse(ErrorCode.BAD_REQUEST.value, "client was not found.")
                        )
                }
                .tapLeft {
                    logger.info { "client was not found. client_id: ${clientId.value}" }
                }
                .bind()
            val redirectUri = request.queryParamOrNull("redirect_uri")
                .let {
                    client.validateRedirectUri(it)
                }
                .mapLeft {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            ErrorResponse(ErrorCode.BAD_REQUEST.value, "invalid redirect_uri.")
                        )
                }
                .bind()
            val state = request.queryParamOrNull("state")
                ?.let {
                    State.of(it)
                        .mapLeft {
                            ServerResponse
                                .status(HttpStatus.FOUND)
                                .location(
                                    DefaultUriBuilderFactory(redirectUri)
                                        .builder()
                                        .queryParam("error", AuthorizationErrorCode.InvalidRequest.value)
                                        .queryParam("error_description", "invalid state value.")
                                        .build()
                                )
                                .buildAndAwait()
                        }.bind()
                }
            val responseType = request.queryParamOrNull("response_type")
            coEnsureNotNull(responseType) {
                ServerResponse
                    .status(HttpStatus.FOUND)
                    .location(
                        DefaultUriBuilderFactory(redirectUri).builder()
                            .apply {
                                queryParam("error", AuthorizationErrorCode.InvalidRequest.value)
                                queryParam("error_description", "response_type must be specified")
                                if (state != null) {
                                    queryParam("state", state.value)
                                }
                            }
                            .build()
                    )
                    .buildAndAwait()
            }
            coEnsure(responseType == "code") {
                ServerResponse
                    .status(HttpStatus.FOUND)
                    .location(
                        DefaultUriBuilderFactory(redirectUri).builder()
                            .apply {
                                queryParam("error", AuthorizationErrorCode.UnsupportedResponseType.value)
                                if (state != null) {
                                    queryParam("state", state.value)
                                }
                            }
                            .build()
                    )
                    .buildAndAwait()
            }

            ServerResponse
                .status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000"))
                .buildAndAwait()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
