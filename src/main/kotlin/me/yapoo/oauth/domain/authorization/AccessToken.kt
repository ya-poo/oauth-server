package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Duration
import java.time.Instant

data class AccessToken(
    val authorizationId: AuthorizationId,
    val accessToken: String,
    val refreshToken: String,
    val issuedAt: Instant,
) {

    val expiresIn: Duration = Duration.ofDays(1)

    companion object {
        private const val TOKEN_LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            authorizationId: AuthorizationId,
            now: Instant,
        ): AccessToken {
            return AccessToken(
                authorizationId = authorizationId,
                accessToken = secureStringFactory.next(TOKEN_LENGTH),
                refreshToken = secureStringFactory.next(TOKEN_LENGTH),
                issuedAt = now
            )
        }
    }
}
