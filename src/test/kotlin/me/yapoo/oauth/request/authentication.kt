package me.yapoo.oauth.request

import me.yapoo.oauth.handler.authentication.AuthenticationRequest
import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.authenticate(
    asi: String,
    email: String = "test@example.com",
    password: String = "password"
): WebTestClient.ResponseSpec {
    return post().uri("/authentication")
        .bodyValue(
            AuthenticationRequest(
                authorizationSessionId = asi,
                email = email,
                password = password
            )
        ).exchange()
}
