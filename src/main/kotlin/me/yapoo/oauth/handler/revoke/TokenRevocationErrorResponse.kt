package me.yapoo.oauth.handler.revoke

import com.fasterxml.jackson.annotation.JsonValue

/**
 * RFC 7009 - 2.2.1
 * see also: TokenErrorResponse
 */
data class TokenRevocationErrorResponse(
    val error: ErrorCode,
    val errorDescription: String,
) {

    // OPTIONAL なのでここでは null とする。
    val errorUri: String? = null

    enum class ErrorCode(val value: String) {
        InvalidRequest("invalid_request"),
        InvalidClient("invalid_client"),
        UnauthorizedClient("unauthorized_client"),
        UnsupportedTokenType("unsupported_token_type"),
        ;

        @JsonValue
        fun value(): String = value
    }
}
