package me.yapoo.oauth.handler.token

// RFC 6749 - 5.1
data class TokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String,
    // クライアントから全く同一なスコープが要求された場合にも返却する
    val scope: List<String>,
) {
    val tokenType: String = "Bearer"
}
