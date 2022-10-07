package me.yapoo.oauth.handler.client

import arrow.core.Either
import arrow.core.continuations.either
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.infrastructure.random.UuidFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.rightIfNotEmpty
import me.yapoo.oauth.router.authentication.client.ClientCredential
import me.yapoo.oauth.router.authentication.client.ClientCredentialRepository
import me.yapoo.oauth.router.error.ErrorCode
import me.yapoo.oauth.router.error.ErrorResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Service
class ClientRegistrationHandler(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val uuidFactory: UuidFactory,
) {

    suspend fun handle(
        request: ServerRequest
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitBody<RegisterClientRequest>()
            coEnsure(body.type == "confidential" || body.type == "public") {
                ServerResponse.badRequest()
                    .bodyValueAndAwait(
                        ErrorResponse(
                            ErrorCode.BAD_REQUEST.value,
                            "type must be confidential or public"
                        )
                    )
            }

            val type = when (body.type) {
                "confidential" -> Client.Type.Confidential
                else -> Client.Type.Public
            }
            coEnsure(clientRepository.findById(ClientId(body.clientId)) == null) {
                ServerResponse.badRequest()
                    .bodyValueAndAwait(
                        ErrorResponse(
                            ErrorCode.BAD_REQUEST.value,
                            "already used client_id"
                        )
                    )
            }
            // TODO: まだスコープを定義していないので、スコープの値のバリデーションはせず、non-empty だけを見ておく。
            val scopes = body.scopes
                .rightIfNotEmpty {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            ErrorResponse(
                                ErrorCode.BAD_REQUEST.value,
                                "scopes must not be empty"
                            )
                        )
                }.bind()
            val redirectUri = body.redirectUri
                .rightIfNotEmpty {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            ErrorResponse(
                                ErrorCode.BAD_REQUEST.value,
                                "redirect_uri must not be empty"
                            )
                        )
                }.bind()

            val client = Client(
                id = ClientId(uuidFactory.next().toString()),
                name = body.name,
                scopes = scopes,
                redirectUris = redirectUri,
                type = type
            )
            clientRepository.save(client)
            clientCredentialRepository.save(
                ClientCredential.new(client.id, body.clientSecret)
            )

            ServerResponse.ok()
                .buildAndAwait()
        }
    }
}
