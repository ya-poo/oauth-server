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
import me.yapoo.oauth.mixin.arrow.rightIfNotEmpty
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
    private val clientRepository: ClientRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

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
                .tapLeft {
                    logger.info { "`client_id` must be specified." }
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
                .tapLeft {
                    logger.info { "invalid redirect_uri: $it. client_id: ${clientId.value}" }
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
                        .tapLeft { _ ->
                            logger.info { "invalid state parameter: $it" }
                        }
                        .mapLeft {
                            ErrorRedirectResponse(
                                redirectUri,
                                AuthorizationErrorCode.InvalidRequest,
                                "invalid state parameter value."
                            )
                        }.bind()
                }
            val responseType = request.queryParamOrNull("response_type")
            coEnsureNotNull(responseType) {
                logger.info { "response_type must be specified" }
                ErrorRedirectResponse(
                    redirectUri,
                    AuthorizationErrorCode.InvalidRequest,
                    "response_type must be specified",
                    state
                )
            }
            coEnsure(responseType == "code") {
                logger.info { "invalid response_type: $responseType" }
                ErrorRedirectResponse(
                    redirectUri,
                    AuthorizationErrorCode.UnsupportedResponseType,
                    "responseType $responseType is not supported",
                    state
                )
            }
            // RFC 6449 - 3.3
            // ここでは `scope` の省略は許可しないことにする。
            val scopes = (request.queryParamOrNull("scope")
                ?.split(" ")
                ?: emptyList())
                .rightIfNotEmpty {
                    ErrorRedirectResponse(
                        redirectUri,
                        AuthorizationErrorCode.InvalidScope,
                        "scope must be specified",
                        state
                    )
                }.bind()
            coEnsure(client.scopes.containsAll(scopes)) {
                ErrorRedirectResponse(
                    redirectUri,
                    AuthorizationErrorCode.InvalidScope,
                    "contains scopes which are not permitted",
                    state
                )
            }

            ServerResponse
                .status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000"))
                .buildAndAwait()
        }
    }

    @Suppress("FunctionName")
    private suspend fun ErrorRedirectResponse(
        redirectUri: String,
        error: AuthorizationErrorCode,
        errorDescription: String,
        state: State? = null,
    ): ServerResponse {
        return ServerResponse
            .status(HttpStatus.FOUND)
            .location(
                DefaultUriBuilderFactory(redirectUri)
                    .builder()
                    .apply {
                        queryParam("error", error.value)
                        queryParam("error_description", errorDescription)
                        if (state != null) {
                            queryParam("state", state.value)
                        }
                    }
                    .build()
            ).buildAndAwait()
    }
}
