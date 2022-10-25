package me.yapoo.oauth.handler.metadata

data class MetadataResponse(
    val issuer: String = "oauth-server",
    val authorizationEndpoint: String = "http://localhost:8080/authorization",
    val tokenEndpoint: String = "http://localhost:8080/token",
    val jwksUri: String? = null,
    val registrationEndpoint: String = "http://localhost:8080/client",
    val scopesSupported: List<String> = listOf("hello", "world", "openid"),
    val responseTypesSupported: List<String> = listOf("code"),
    val responseModesSupported: List<String> = listOf("query"),
    val grantTypesSupported: List<String> = listOf("authorization_code"),
    val tokenEndpointAuthMethodsSupported: List<String> = listOf("client_secret_basic"),
    val tokenEndpointAuthSigningAlgValuesSupported: List<String>? = null,
    val serviceDocumentation: String? = null,
    val uiLocalesSupported: List<String> = listOf("ja"),
    val opPolicyUri: String? = null,
    val opTosUri: String? = null,
    val revocationEndpoint: String = "http://localhost:8080/revoke",
    val revocationEndpointAuthMethodsSupported: List<String> = listOf("client_secret_basic"),
    val codeChallengeMethodsSupported: List<String> = listOf("plain", "S256")
)
