package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.user.UserSubject

data class Authorization(
    val id: AuthorizationId,
    val userSubject: UserSubject,
    val clientId: ClientId,
    // ここには `openid` は入れない
    val scopes: List<String>,
) {

    companion object {

        fun new(
            id: AuthorizationId,
            userSubject: UserSubject,
            clientId: ClientId,
            scopes: List<String>,
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
