package me.yapoo.oauth.request

import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.authorization(
    clientId: String,
    redirectUri: String,
    responseType: String = "code",
    scope: String = "hello",
    state: String? = null
): WebTestClient.ResponseSpec {
    return get().uri {
        it.path("/authorization")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", responseType)
            .queryParam("scope", scope)
            .queryParam("state", state)
            .build()
    }.exchange()
}
