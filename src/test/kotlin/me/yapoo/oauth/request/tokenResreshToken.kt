package me.yapoo.oauth.request

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

fun WebTestClient.tokenRefreshToken(
    clientId: String,
    clientSecret: String,
    refreshToken: String,
): WebTestClient.ResponseSpec {
    return post().uri("/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .headers {
            it.setBasicAuth(clientId, clientSecret)
        }
        .body(
            BodyInserters
                .fromFormData("grant_type", "refresh_token")
                .with("refresh_token", refreshToken)
        ).exchange()
}
