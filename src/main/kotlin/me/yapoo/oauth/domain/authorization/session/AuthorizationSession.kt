package me.yapoo.oauth.domain.authorization.session

import me.yapoo.oauth.domain.authorization.State

data class AuthorizationSession(
    val id: AuthorizationSessionId,
    val scopes: List<String>,
    val state: State?,
)
