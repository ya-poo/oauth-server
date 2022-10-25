package me.yapoo.oauth.handler.deviceAuthorization

import com.fasterxml.jackson.annotation.JsonValue

// RFC 8628 - 3.2
data class DeviceAuthorizationErrorResponse(
    val error: ErrorCode,
    val errorDescription: String,
) {
    enum class ErrorCode(
        @JsonValue
        val value: String
    ) {
        InvalidRequest("invalid_request"),
        InvalidClient("invalid_client"),
        InvalidScope("invalid_scope"),
    }
}
