package me.yapoo.oauth.request

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

fun WebTestClient.revokeToken(
    token: String,
    clientId: String,
    clientSecret: String,
): WebTestClient.ResponseSpec {
    return post().uri("/revoke")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .headers {
            it.setBasicAuth(clientId, clientSecret)
        }
        .body(
            BodyInserters
                .fromFormData("token", token)
        ).exchange()
}
