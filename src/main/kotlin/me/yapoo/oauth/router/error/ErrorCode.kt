package me.yapoo.oauth.router.error

// RFC に規定がない場合に用いるエラーコード
enum class ErrorCode(val value: String) {
    BAD_REQUEST("invalid_request"),
    NOT_FOUND("not_found")
}
