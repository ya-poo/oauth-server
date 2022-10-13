package me.yapoo.oauth.handler.token

// RFC 6749 - 5.1
data class TokenRefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String,
    val scope: List<String>,
) {
    val tokenType: String = "Bearer"
}
