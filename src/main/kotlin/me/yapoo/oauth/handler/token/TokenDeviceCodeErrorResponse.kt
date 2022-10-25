package me.yapoo.oauth.handler.token

import com.fasterxml.jackson.annotation.JsonValue

// RFC 8628 - 3.5
data class TokenDeviceCodeErrorResponse(
    val error: ErrorCode,
    val errorDescription: String,
) {

    // slow_down は実装しない
    enum class ErrorCode(
        @JsonValue
        val value: String
    ) {
        InvalidRequest("invalid_request"),
        InvalidClient("invalid_client"),
        AccessDenied("access_denied"),
        ExpiredToken("expired_token"),
        AuthorizationPending("authorization_pending"),
    }
}
