package me.yapoo.oauth.router.authentication.token

// RFC 6750 - 3.1
enum class BearerTokenErrorCode(val value: String) {
    InvalidRequest("invalid_request"),
    InvalidToken("invalid_token"),
    InsufficientScope("insufficient_scope")
}
