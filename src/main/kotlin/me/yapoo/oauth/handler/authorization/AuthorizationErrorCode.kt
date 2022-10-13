package me.yapoo.oauth.handler.authorization

// RFC 6749 4.1.2.1
// OpenId Connect Core 1.0
enum class AuthorizationErrorCode(val value: String) {
    InvalidRequest("invalid_request"),
    UnauthorizedClient("unauthorized_client"),
    AccessDenied("access_denied"),
    UnsupportedResponseType("unsupported_response_type"),
    InvalidScope("invalid_scope"),
    ServerError("server_error"),
    TemporarilyUnavailable("temporarily_unavailable"),

    // 以下は OpenId Connect Core 1.0 で定義されたエラーコード
    InteractionRequired("interaction_required"),
    LoginRequired("login_required"),
    AccountSelectionRequired("account_selection_required"),
    ConsentRequired("consent_required"),
    InvalidRequestUri("invalid_request_uri"),
    InvalidRequestObject("invalid_request_object"),
    RequestNotSupported("request_not_supported"),
    RequestUriNotSupported("request_uri_not_supported"),
    RegistrationNotSupported("request_uri_not_supported"),
}
