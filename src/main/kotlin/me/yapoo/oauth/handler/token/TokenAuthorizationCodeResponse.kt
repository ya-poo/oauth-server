package me.yapoo.oauth.handler.token

// RFC 6749 - 5.1
// OpenID Connect Core - 3.1.3.3
data class TokenAuthorizationCodeResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String,
    val scope: List<String>,
    val idToken: String?
) {
    val tokenType: String = "Bearer"
}
