package me.yapoo.oauth.handler.token

// RFC 8628 - 3.5 (RFC 6749 - 5.1)
// リフレッシュトークンの発行は任意なので、ここでは発行しない
data class TokenDeviceCodeResponse(
    val accessToken: String,
    val expiresIn: Int,
    val scope: List<String>,
) {
    val tokenType: String = "Bearer"
}
