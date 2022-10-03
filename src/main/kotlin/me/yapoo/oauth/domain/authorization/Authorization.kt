package me.yapoo.oauth.domain.authorization

import arrow.core.NonEmptyList
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.user.UserSubject
import me.yapoo.oauth.infrastructure.random.SecureStringFactory
import java.time.Instant

data class Authorization(
    val id: AuthorizationId,
    val userSubject: UserSubject,
    val clientId: ClientId,
    val scopes: NonEmptyList<String>,
    val accessToken: String,
    val refreshToken: String,
    val issuedAt: Instant
) {

    companion object {
        private const val TOKEN_LENGTH = 50

        fun new(
            secureStringFactory: SecureStringFactory,
            id: AuthorizationId,
            userSubject: UserSubject,
            clientId: ClientId,
            scopes: NonEmptyList<String>,
            now: Instant,
        ): Authorization {
            return Authorization(
                id = id,
                userSubject = userSubject,
                clientId = clientId,
                scopes = scopes,
                accessToken = secureStringFactory.next(TOKEN_LENGTH),
                refreshToken = secureStringFactory.next(TOKEN_LENGTH),
                issuedAt = now
            )
        }
    }
}
