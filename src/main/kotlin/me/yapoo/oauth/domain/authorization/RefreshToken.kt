package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Duration
import java.time.Instant

data class RefreshToken(
    val value: String,
    val authorizationId: AuthorizationId,
    val issuedAt: Instant,
) {
    val expiresAt: Instant = issuedAt + expiresIn

    fun next(
        secureStringFactory: SecureStringFactory,
        now: Instant,
    ): RefreshToken {
        return RefreshToken(
            secureStringFactory.next(TOKEN_LENGTH),
            authorizationId,
            now
        )
    }

    companion object {
        val expiresIn: Duration = Duration.ofDays(1)

        private const val TOKEN_LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            authorizationId: AuthorizationId,
            now: Instant,
        ): RefreshToken {
            return RefreshToken(
                value = secureStringFactory.next(TOKEN_LENGTH),
                authorizationId = authorizationId,
                issuedAt = now
            )
        }
    }
}
