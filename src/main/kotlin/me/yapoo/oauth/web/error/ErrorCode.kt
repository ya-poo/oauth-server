package me.yapoo.oauth.web.error

enum class ErrorCode(val value: String) {
    BAD_REQUEST("invalid_request"),
    NOT_FOUND("not_found")
}
