package me.yapoo.oauth.handler.client

data class RegisterClientResponse(
    val name: String,
    val clientId: String,
    val clientSecret: String,
    val scopes: List<String>,
    val redirectUris: List<String>,
)
