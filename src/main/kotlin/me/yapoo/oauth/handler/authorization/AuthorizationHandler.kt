package me.yapoo.oauth.handler.authorization

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.State
import me.yapoo.oauth.domain.authorization.session.AuthorizationSession
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.coEnsureNotNull
import me.yapoo.oauth.mixin.arrow.rightIfNotEmpty
import me.yapoo.oauth.router.error.ErrorCode
import me.yapoo.oauth.router.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class AuthorizationHandler(
    private val clientRepository: ClientRepository,
    private val secureStringFactory: SecureStringFactory,
    private val authorizationSessionRepository: AuthorizationSessionRepository,
) {

    // 認可エンドポイント (RFC 6749 - 3.1)
    // 現在は認可コードフロー (RFC 6749 - 4.1) のみ対応
    // TODO : いくつかのリクエスト値の値のフォーマット (文字種) の検査
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
                            ErrorRedirectResponse(
                                redirectUri,
                                AuthorizationErrorCode.InvalidRequest,
                                "invalid state parameter value."
                            )
                        }.bind()
                }
            val responseType = request.queryParamOrNull("response_type")
            coEnsureNotNull(responseType) {
                ErrorRedirectResponse(
                    redirectUri,
                    AuthorizationErrorCode.InvalidRequest,
                    "response_type must be specified",
                    state
                )
            }
            coEnsure(responseType == "code") {
                ErrorRedirectResponse(
                    redirectUri,
                    AuthorizationErrorCode.UnsupportedResponseType,
                    "responseType $responseType is not supported",
                    state
                )
            }
            // RFC 6749 - 3.3
            // ここでは `scope` の省略は許可しないことにする。
            val scopes = (
                request.queryParamOrNull("scope")
                    ?.split(" ")
                    ?: emptyList()
                )
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

            val authorizationSession = AuthorizationSession(
                id = AuthorizationSessionId.next(secureStringFactory),
                clientId = clientId,
                redirectUri = redirectUri,
                scopes = scopes,
                state = state,
                redirectUriSpecified = request.queryParamOrNull("redirect_uri") != null
            )
            authorizationSessionRepository.add(authorizationSession)

            // TODO: asi は暗号化する, scope も含める。JWE にしたい。
            ServerResponse
                .status(HttpStatus.FOUND)
                .location(
                    DefaultUriBuilderFactory("http://localhost:3000")
                        .builder()
                        .queryParam("asi", authorizationSession.id.value)
                        .build()
                )
                .buildAndAwait()
        }
    }

    // RFC 6749 4.1.2.1
    // ここでは `error_uri` は設定しない。
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
