package me.yapoo.oauth.domain.authorization

import arrow.core.NonEmptyList
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.user.UserSubject

data class Authorization(
    val id: AuthorizationId,
    val userSubject: UserSubject,
    val clientId: ClientId,
    val scopes: NonEmptyList<String>,
) {

    companion object {

        fun new(
            id: AuthorizationId,
            userSubject: UserSubject,
            clientId: ClientId,
            scopes: NonEmptyList<String>,
        ): Authorization {
            return Authorization(
                id = id,
                userSubject = userSubject,
                clientId = clientId,
                scopes = scopes,
            )
        }
    }
}
