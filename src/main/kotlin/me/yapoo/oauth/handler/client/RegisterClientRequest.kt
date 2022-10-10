package me.yapoo.oauth.handler.client

data class RegisterClientRequest(
    val type: String,
    val name: String,
    val scopes: List<String>,
    val redirectUris: List<String>,
)
