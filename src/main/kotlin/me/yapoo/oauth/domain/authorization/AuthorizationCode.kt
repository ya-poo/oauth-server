package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Instant

data class AuthorizationCode(
    val value: String,
    val authorizationId: AuthorizationId,
    val issuedAt: Instant
) {

    companion object {

        private const val LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            authorizationId: AuthorizationId,
            now: Instant
        ): AuthorizationCode {
            return AuthorizationCode(
                value = secureStringFactory.next(LENGTH),
                authorizationId = authorizationId,
                issuedAt = now
            )
        }
    }
}
