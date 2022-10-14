package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Duration
import java.time.Instant

data class AccessToken(
    val value: String,
    val authorizationId: AuthorizationId,
    val issuedAt: Instant,
) {
    val expiresAt: Instant = issuedAt + expiresIn

    companion object {
        val expiresIn: Duration = Duration.ofMinutes(30)

        private const val TOKEN_LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            authorizationId: AuthorizationId,
            now: Instant,
        ): AccessToken {
            return AccessToken(
                authorizationId = authorizationId,
                value = secureStringFactory.next(TOKEN_LENGTH),
                issuedAt = now
            )
        }
    }
}
