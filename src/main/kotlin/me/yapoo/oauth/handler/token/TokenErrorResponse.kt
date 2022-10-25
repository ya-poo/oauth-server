package me.yapoo.oauth.handler.token

import com.fasterxml.jackson.annotation.JsonValue

// RFC 6749 - 5.2
data class TokenErrorResponse(
    val error: ErrorCode,
    val errorDescription: String,
) {

    // OPTIONAL なのでここでは null とする。
    val errorUri: String? = null

    enum class ErrorCode(
        @JsonValue
        val value: String
    ) {
        InvalidRequest("invalid_request"),
        InvalidClient("invalid_client"),
        InvalidGrant("invalid_grant"),
        UnauthorizedClient("unauthorized_client"),
        UnsupportedGrantType("unsupported_grant_type"),
        InvalidScope("invalid_scope"),

        // 以下は RFC 6749 - 5.2 で定められていないエラーコード。
        // エラーコードの種類は限定されているともいないとも読めるため、
        // ここでは指定されていないエラーコードも用いることにしておく。
        InternalServerError("internal_server_error"),
        ClientNotFound("client_not_found"),
        ;
    }
}
