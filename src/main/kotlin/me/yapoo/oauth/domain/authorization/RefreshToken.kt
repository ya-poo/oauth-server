package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Instant

data class RefreshToken(
    val value: String,
    val authorizationId: AuthorizationId,
    val issuedAt: Instant,
) {

    companion object {
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
