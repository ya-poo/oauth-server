package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Duration
import java.time.Instant

data class AuthorizationCode(
    val value: String,
    val authorizationId: AuthorizationId,
    val authorizationSessionId: AuthorizationSessionId,
    val issuedAt: Instant
) {

    private val expiresAt: Instant = issuedAt + LIFETIME

    fun isExpired(
        now: Instant
    ): Boolean = expiresAt <= now

    companion object {
        private val LIFETIME = Duration.ofMinutes(5)

        private const val LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            authorizationId: AuthorizationId,
            authorizationSessionId: AuthorizationSessionId,
            now: Instant
        ): AuthorizationCode {
            return AuthorizationCode(
                value = secureStringFactory.next(LENGTH),
                authorizationId = authorizationId,
                authorizationSessionId = authorizationSessionId,
                issuedAt = now
            )
        }
    }
}
