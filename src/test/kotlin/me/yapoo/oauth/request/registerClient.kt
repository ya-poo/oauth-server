package me.yapoo.oauth.request

import me.yapoo.oauth.handler.client.RegisterClientRequest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.registerClient(
    type: String = "confidential",
    name: String = "test",
    scopes: List<String> = listOf("hello"),
    redirectUris: List<String> = listOf("https://example.com")
): WebTestClient.ResponseSpec {
    return post().uri("/client")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            RegisterClientRequest(
                type = type,
                name = name,
                scopes = scopes,
                redirectUris = redirectUris
            )
        ).exchange()
}
