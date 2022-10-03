package me.yapoo.oauth.domain.authorization.session

import arrow.core.NonEmptyList
import me.yapoo.oauth.domain.authorization.State
import me.yapoo.oauth.domain.client.ClientId

data class AuthorizationSession(
    val id: AuthorizationSessionId,
    val clientId: ClientId,
    val scopes: NonEmptyList<String>,
    val state: State?,
    val redirectUri: String,
)
