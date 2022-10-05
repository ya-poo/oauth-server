package me.yapoo.oauth.handler.authentication

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.Authorization
import me.yapoo.oauth.domain.authorization.AuthorizationCode
import me.yapoo.oauth.domain.authorization.AuthorizationCodeRepository
import me.yapoo.oauth.domain.authorization.AuthorizationId
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import me.yapoo.oauth.domain.user.UserCredentialRepository
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import me.yapoo.oauth.infrastructure.random.UuidFactory
import me.yapoo.oauth.infrastructure.time.DateTimeFactory
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.router.error.ErrorCode
import me.yapoo.oauth.router.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class AuthenticationHandler(
    private val authorizationSessionRepository: AuthorizationSessionRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val authorizationRepository: AuthorizationRepository,
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val secureStringFactory: SecureStringFactory,
    private val dateTimeFactory: DateTimeFactory,
    private val uuidFactory: UuidFactory,
) {

    suspend fun handle(
        request: ServerRequest
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitBody<AuthenticationRequest>()

            val authorizationSession = body.authorizationSessionId
                .let(::AuthorizationSessionId)
                .let { authorizationSessionRepository.findById(it) }
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.NOT_FOUND).bodyValueAndAwait(
                        ErrorResponse(
                            ErrorCode.NOT_FOUND.value,
                            "authorization session was not found"
                        )
                    )
                }.bind()

            val userCredential = userCredentialRepository.findByEmail(body.email)
                .rightIfNotNull {
                    ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                        ErrorResponse(
                            ErrorCode.BAD_REQUEST.value,
                            "email or password is invalid."
                        )
                    )
                }.bind()
            coEnsure(userCredential.accepts(body.password)) {
                ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(
                    ErrorResponse(
                        ErrorCode.BAD_REQUEST.value,
                        "email or password is invalid."
                    )
                )
            }
            val now = dateTimeFactory.now()
            val authorizationId = AuthorizationId.new(uuidFactory)
            val authorization = Authorization.new(
                id = authorizationId,
                userSubject = userCredential.id,
                clientId = authorizationSession.clientId,
                scopes = authorizationSession.scopes,
            )
            authorizationRepository.save(authorization)

            val authorizationCode = AuthorizationCode.new(
                secureStringFactory,
                authorizationId,
                authorizationSession.id,
                now
            )
            authorizationCodeRepository.save(authorizationCode)

            // RFC 6749 4.1.2
            ServerResponse.status(HttpStatus.FOUND)
                .location(
                    DefaultUriBuilderFactory(authorizationSession.redirectUri).builder().apply {
                        queryParam("code", authorizationCode.value)
                        if (authorizationSession.state != null) {
                            queryParam("state", authorizationSession.state.value)
                        }
                    }.build()
                ).buildAndAwait()
        }
    }
}
