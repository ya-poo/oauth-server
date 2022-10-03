package me.yapoo.oauth.handler.authentication

data class AuthenticationRequest(
    val authorizationSessionId: String,
    val email: String,
    val password: String,
)
