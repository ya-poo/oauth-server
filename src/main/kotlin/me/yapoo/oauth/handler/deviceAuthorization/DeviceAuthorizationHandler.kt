package me.yapoo.oauth.handler.deviceAuthorization

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSession
import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSessionId
import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSessionRepository
import me.yapoo.oauth.domain.device.session.UserCode
import me.yapoo.oauth.domain.device.session.UserCodeRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.time.SystemClock
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.arrow.rightIfNotEmpty
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class DeviceAuthorizationHandler(
    private val systemClock: SystemClock,
    private val secureStringFactory: SecureStringFactory,
    private val clientRepository: ClientRepository,
    private val deviceAuthorizationSessionRepository: DeviceAuthorizationSessionRepository,
    private val userCodeRepository: UserCodeRepository,
) {

    // OAuth 2.0 デバイスフローにおける認可エンドポイント (RFC 8628)
    // TODO: 認証を通常の認可コードフローと統合できないか検討する。
    suspend fun handle(
        request: ServerRequest,
        authenticatedClient: Client?
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitFormData()

            // ここでは通常の認可エンドポイントと同様に `scope` の省略は許可しない
            val scopes = (body.getSingle("scope")?.split(" ") ?: emptyList())
                .rightIfNotEmpty {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            DeviceAuthorizationErrorResponse(
                                error = DeviceAuthorizationErrorResponse.ErrorCode.InvalidScope,
                                errorDescription = "scope must not be empty"
                            )
                        )
                }.bind()
            val client = authenticatedClient ?: run {
                val clientId = body.getSingle("client_id")?.let(::ClientId)
                    .rightIfNotNull {
                        ServerResponse.badRequest()
                            .bodyValueAndAwait(
                                DeviceAuthorizationErrorResponse(
                                    error = DeviceAuthorizationErrorResponse.ErrorCode.InvalidClient,
                                    errorDescription = "client authentication or client_id is needed"
                                )
                            )
                    }.bind()
                clientRepository.findById(clientId)
                    .rightIfNotNull {
                        ServerResponse.badRequest()
                            .bodyValueAndAwait(
                                DeviceAuthorizationErrorResponse(
                                    error = DeviceAuthorizationErrorResponse.ErrorCode.InvalidClient,
                                    errorDescription = "client_id is invalid"
                                )
                            )
                    }.bind()
            }

            coEnsure(client.scopes.containsAll(scopes)) {
                ServerResponse.badRequest()
                    .bodyValueAndAwait(
                        DeviceAuthorizationErrorResponse(
                            error = DeviceAuthorizationErrorResponse.ErrorCode.InvalidScope,
                            errorDescription = "scope contains invalid value"
                        )
                    )
            }

            val deviceAuthorizationSession = DeviceAuthorizationSession(
                id = DeviceAuthorizationSessionId.next(secureStringFactory),
                clientId = client.id,
                scopes = scopes,
                deviceCode = secureStringFactory.next(30),
                issuedAt = systemClock.now()
            )
            deviceAuthorizationSessionRepository.add(deviceAuthorizationSession)

            val userCode = UserCode.new(deviceAuthorizationSession.id)
            userCodeRepository.add(userCode)

            ServerResponse.ok()
                .bodyValueAndAwait(
                    DeviceAuthorizationResponse(
                        deviceCode = deviceAuthorizationSession.deviceCode,
                        userCode = userCode.value,
                        verificationUri = "http://localhost:3000",
                        expiresIn = deviceAuthorizationSession.expiresIn.toSeconds().toInt(),
                    )
                )
        }
    }
}
