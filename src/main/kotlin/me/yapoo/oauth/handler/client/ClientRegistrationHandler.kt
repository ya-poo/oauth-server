package me.yapoo.oauth.handler.client

import arrow.core.Either
import arrow.core.continuations.either
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
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

@Service
class ClientRegistrationHandler(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val uuidFactory: UuidFactory,
    private val secureStringFactory: SecureStringFactory,
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
            val redirectUri = body.redirectUris
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
            clientRepository.add(client)
            val plainCredential = secureStringFactory.next(ClientCredential.CREDENTIAL_LENGTH)
            val clientCredential = ClientCredential.new(client.id, plainCredential)
            clientCredentialRepository.add(clientCredential)

            ServerResponse.ok()
                .bodyValueAndAwait(
                    RegisterClientResponse(
                        name = client.name,
                        clientId = client.id.value,
                        clientSecret = plainCredential,
                        scopes = scopes,
                        redirectUris = redirectUri
                    )
                )
        }
    }
}
