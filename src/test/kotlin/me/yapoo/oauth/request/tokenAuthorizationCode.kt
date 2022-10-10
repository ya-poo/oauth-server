package me.yapoo.oauth.request

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

fun WebTestClient.tokenAuthorizationCode(
    clientId: String,
    clientSecret: String,
    code: String,
    redirectUri: String,
): WebTestClient.ResponseSpec {
    return post().uri("/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .headers {
            it.setBasicAuth(clientId, clientSecret)
        }
        .body(
            BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("code", code)
                .with("redirect_uri", redirectUri)
                .with("client_id", clientId)
        ).exchange()
}
