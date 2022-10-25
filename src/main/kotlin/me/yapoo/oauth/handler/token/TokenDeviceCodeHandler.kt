package me.yapoo.oauth.handler.token

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.Authorization
import me.yapoo.oauth.domain.authorization.AuthorizationId
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSessionRepository
import me.yapoo.oauth.domain.device.session.UserCodeRepository
import me.yapoo.oauth.domain.user.UserSubject
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.random.UuidFactory
import me.yapoo.oauth.infrastructure.time.SystemClock
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class TokenDeviceCodeHandler(
    private val systemClock: SystemClock,
    private val deviceAuthorizationSessionRepository: DeviceAuthorizationSessionRepository,
    private val userCodeRepository: UserCodeRepository,
    private val clientRepository: ClientRepository,
    private val authorizationRepository: AuthorizationRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val uuidFactory: UuidFactory,
    private val secureStringFactory: SecureStringFactory,
) {

    // トークンエンドポイント (RFC 8628 - 3.4)
    // デバイスフロー用のトークンエンドポイント
    suspend fun handle(
        request: ServerRequest,
        authenticatedClient: Client?,
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitFormData()
            val deviceCode = body.getSingle("device_code")
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenDeviceCodeErrorResponse(
                            error = TokenDeviceCodeErrorResponse.ErrorCode.InvalidRequest,
                            errorDescription = "device_code must be specified"
                        )
                    )
                }.bind()

            val authorizationSession = deviceAuthorizationSessionRepository.findByDeviceCode(deviceCode)
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenDeviceCodeErrorResponse(
                            error = TokenDeviceCodeErrorResponse.ErrorCode.AccessDenied,
                            errorDescription = "invalid device_code"
                        )
                    )
                }
                .bind()

            val client = authenticatedClient ?: run {
                val clientId = body.getSingle("client_id")?.let(::ClientId).rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenDeviceCodeErrorResponse(
                            error = TokenDeviceCodeErrorResponse.ErrorCode.InvalidClient,
                            errorDescription = "client_id must be specified"
                        )
                    )
                }.bind()
                clientRepository.findById(clientId).rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenDeviceCodeErrorResponse(
                            error = TokenDeviceCodeErrorResponse.ErrorCode.InvalidClient,
                            errorDescription = "invalid client"
                        )
                    )
                }.bind()
            }
            coEnsure(authorizationSession.clientId == client.id) {
                ServerResponse.badRequest().bodyValueAndAwait(
                    TokenDeviceCodeErrorResponse(
                        error = TokenDeviceCodeErrorResponse.ErrorCode.InvalidClient,
                        errorDescription = "invalid client"
                    )
                )
            }

            val now = systemClock.now()
            coEnsure(now < authorizationSession.issuedAt + authorizationSession.expiresIn) {
                ServerResponse.badRequest().bodyValueAndAwait(
                    TokenDeviceCodeErrorResponse(
                        error = TokenDeviceCodeErrorResponse.ErrorCode.ExpiredToken,
                        errorDescription = "device_code is expired"
                    )
                )
            }

            val userCode = userCodeRepository.findByDeviceAuthorizationSessionId(authorizationSession.id)
                .rightIfNotNull {
                    ServerResponse.badRequest().bodyValueAndAwait(
                        TokenDeviceCodeErrorResponse(
                            error = TokenDeviceCodeErrorResponse.ErrorCode.AccessDenied,
                            errorDescription = "invalid device_code"
                        )
                    )
                }
                .bind()
            coEnsure(userCode.confirmed) {
                ServerResponse.badRequest().bodyValueAndAwait(
                    TokenDeviceCodeErrorResponse(
                        error = TokenDeviceCodeErrorResponse.ErrorCode.AuthorizationPending,
                        errorDescription = "authentication is not completed"
                    )
                )
            }

            // TODO: UserSubject を決めるために、認証時に何らかの情報を保存しておく必要がある。
            val authorizationId = AuthorizationId.new(uuidFactory)
            val authorization = Authorization.new(
                id = authorizationId,
                userSubject = UserSubject("TODO"),
                clientId = authorizationSession.clientId,
                scopes = authorizationSession.scopes,
            )
            authorizationRepository.add(authorization)

            val accessToken = AccessToken.new(
                secureStringFactory,
                authorization.id,
                now
            )
            accessTokenRepository.add(accessToken)

            ServerResponse.ok()
                .headers {
                    it.cacheControl = "no-store"
                    it.pragma = "no-cache"
                }
                .bodyValueAndAwait(
                    TokenDeviceCodeResponse(
                        accessToken = accessToken.value,
                        expiresIn = AccessToken.expiresIn.seconds.toInt(),
                        scope = authorization.scopes,
                    )
                )
        }
    }
}
