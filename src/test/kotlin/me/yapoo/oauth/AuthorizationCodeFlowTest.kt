@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package me.yapoo.oauth

import me.yapoo.oauth.handler.authentication.AuthenticationRequest
import me.yapoo.oauth.handler.client.RegisterClientRequest
import me.yapoo.oauth.handler.client.RegisterClientResponse
import me.yapoo.oauth.handler.token.TokenResponse
import me.yapoo.oauth.mixin.queryParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
class AuthorizationCodeFlowTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun 認可コードフロー() {
        val client = webTestClient.post().uri("/client")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                RegisterClientRequest(
                    type = "confidential",
                    name = "test",
                    scopes = listOf("hello"),
                    redirectUris = listOf("https://example.com")
                )
            ).exchange().expectBody<RegisterClientResponse>().returnResult().responseBody!!

        val state = UUID.randomUUID().toString()
        val authorizationResponseLocation = webTestClient.get().uri {
            it.path("/authorization")
                .queryParam("client_id", client.clientId)
                .queryParam("redirect_uri", "https://example.com")
                .queryParam("state", state)
                .queryParam("response_type", "code")
                .queryParam("scope", "hello")
                .build()
        }.exchange().expectBody<Unit>().returnResult().responseHeaders["Location"]?.singleOrNull()!!
        val asi = URI.create(authorizationResponseLocation).queryParams["asi"]?.singleOrNull()!!

        val authenticationResponseLocation = webTestClient.post().uri("/authentication")
            .bodyValue(
                AuthenticationRequest(
                    authorizationSessionId = asi,
                    email = "test@example.com",
                    password = "password"
                )
            ).exchange().expectBody<Unit>().returnResult().responseHeaders["Location"]?.singleOrNull()!!
            .let(URI::create)
        val code = authenticationResponseLocation.queryParams["code"]?.singleOrNull()!!
        assertEquals(state, authenticationResponseLocation.queryParams["state"]?.singleOrNull())

        val tokenResponse = webTestClient.post().uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .headers {
                it.setBasicAuth(client.clientId, client.clientSecret)
            }
            .body(
                BodyInserters
                    .fromFormData("grant_type", "authorization_code")
                    .with("code", code)
                    .with("redirect_uri", "https://example.com")
                    .with("client_id", client.clientId)
            ).exchange().expectBody<TokenResponse>().returnResult().responseBody!!

        webTestClient.get().uri("/hello")
            .headers {
                it.setBearerAuth(tokenResponse.accessToken)
            }.exchange().apply {
                expectStatus().isOk
                expectBody().json("""{"language":"日本語", "value":"こんにちは"}""")
            }

        webTestClient.get().uri("/world")
            .headers {
                it.setBearerAuth(tokenResponse.accessToken)
            }.exchange().apply {
                expectStatus().isForbidden
                expectHeader().values("WWW-Authenticate") {
                    assertTrue(it.contains("Bearer realm=\"oauth-server\""))
                    assertTrue(it.contains("error=\"insufficient_scope\""))
                    assertTrue(it.contains("scope=world"))
                }
            }

        val refreshTokenResponse = webTestClient.post().uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .headers {
                it.setBasicAuth(client.clientId, client.clientSecret)
            }
            .body(
                BodyInserters
                    .fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", tokenResponse.refreshToken)
            ).exchange().expectBody<TokenResponse>().returnResult().responseBody!!
        
        webTestClient.get().uri("/hello")
            .headers {
                it.setBearerAuth(tokenResponse.accessToken)
            }.exchange().apply {
                expectStatus().isUnauthorized
                expectHeader().values("WWW-Authenticate") {
                    assertTrue(it.contains("Bearer realm=\"oauth-server\""))
                    assertTrue(it.contains("error=\"invalid_token\""))
                }
            }

        webTestClient.get().uri("/hello")
            .headers {
                it.setBearerAuth(refreshTokenResponse.accessToken)
            }.exchange().apply {
                expectStatus().isOk
                expectBody().json("""{"language":"日本語", "value":"こんにちは"}""")
            }
    }
}
