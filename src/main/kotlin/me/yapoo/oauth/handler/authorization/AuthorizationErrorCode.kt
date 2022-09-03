package me.yapoo.oauth.handler.authorization

// RFC 6749 4.1.2.1
enum class AuthorizationErrorCode(val value: String) {
    InvalidRequest("invalid_request"),
    UnauthorizedClient("unauthorized_client"),
    AccessDenied("access_denied"),
    UnsupportedResponseType("unsupported_response_type"),
    InvalidScope("invalid_scope"),
    ServerError("server_error"),
    TemporarilyUnavailable("temporarily_unavailable"),
}
