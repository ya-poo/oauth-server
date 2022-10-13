package me.yapoo.oauth.request

import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.authorization(
    clientId: String,
    redirectUri: String,
    responseType: String = "code",
    scope: String = "hello",
    state: String? = null,
    codeChallenge: String? = null,
    codeChallengeMethod: String? = null,
    nonce: String? = null,
): WebTestClient.ResponseSpec {
    return get().uri {
        it.path("/authorization").apply {
            queryParam("client_id", clientId)
            queryParam("redirect_uri", redirectUri)
            queryParam("response_type", responseType)
            queryParam("scope", scope)
            if (state != null) {
                queryParam("state", state)
            }
            if (codeChallenge != null) {
                queryParam("code_challenge", codeChallenge)
            }
            if (codeChallengeMethod != null) {
                queryParam("code_challenge_method", codeChallengeMethod)
            }
            if (nonce != null) {
                queryParam("nonce", nonce)
            }
        }.build()
    }.exchange()
}
