package me.yapoo.oauth.router.error

enum class ErrorCode(val value: String) {
    BAD_REQUEST("invalid_request"),
    NOT_FOUND("not_found")
}
