package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.domain.user.UserSubject
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Duration
import java.time.Instant

// TODO: これが UserSubject を持つのはやや気持ち悪い?
data class AuthorizationCode(
    val value: String,
    val userSubject: UserSubject,
    val authorizationSessionId: AuthorizationSessionId,
    val issuedAt: Instant
) {

    val expiresAt: Instant = issuedAt + LIFETIME

    companion object {
        private val LIFETIME = Duration.ofMinutes(5)

        private const val LENGTH = 30

        fun new(
            secureStringFactory: SecureStringFactory,
            userSubject: UserSubject,
            authorizationSessionId: AuthorizationSessionId,
            now: Instant
        ): AuthorizationCode {
            return AuthorizationCode(
                value = secureStringFactory.next(LENGTH),
                userSubject = userSubject,
                authorizationSessionId = authorizationSessionId,
                issuedAt = now
            )
        }
    }
}
