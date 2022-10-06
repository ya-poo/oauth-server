package me.yapoo.oauth.handler.client

data class RegisterClientRequest(
    val type: String,
    val name: String,
    val clientId: String,
    val clientSecret: String,
    val scopes: List<String>,
    val redirectUri: List<String>,
)
