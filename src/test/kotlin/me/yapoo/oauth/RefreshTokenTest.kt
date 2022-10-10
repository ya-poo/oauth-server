@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package me.yapoo.oauth

import me.yapoo.oauth.handler.client.RegisterClientResponse
import me.yapoo.oauth.handler.token.TokenResponse
import me.yapoo.oauth.mixin.queryParams
import me.yapoo.oauth.request.authenticate
import me.yapoo.oauth.request.authorization
import me.yapoo.oauth.request.registerClient
import me.yapoo.oauth.request.tokenAuthorizationCode
import me.yapoo.oauth.request.tokenRefreshToken
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
class RefreshTokenTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `リフレッシュトークンで新規トークンが発行され、既存のトークンは使えなくなる`() {
        val client = webTestClient.registerClient().expectBody<RegisterClientResponse>().returnResult().responseBody!!
        val authorizationResponseLocation = webTestClient.authorization(
            clientId = client.clientId,
            redirectUri = client.redirectUris.first(),
        ).expectBody<Unit>().returnResult().responseHeaders.location!!
        val asi = authorizationResponseLocation.queryParams["asi"]?.singleOrNull()!!

        val authenticationResponseLocation =
            webTestClient.authenticate(asi = asi).expectBody<Unit>().returnResult().responseHeaders.location!!
        val code = authenticationResponseLocation.queryParams["code"]?.singleOrNull()!!

        val tokenResponse = webTestClient.tokenAuthorizationCode(
            clientId = client.clientId,
            clientSecret = client.clientSecret,
            code = code,
            redirectUri = client.redirectUris.first(),
        ).expectBody<TokenResponse>().returnResult().responseBody!!

        val refreshTokenResponse = webTestClient.tokenRefreshToken(
            clientId = client.clientId,
            clientSecret = client.clientSecret,
            refreshToken = tokenResponse.refreshToken
        ).expectBody<TokenResponse>().returnResult().responseBody!!

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

        webTestClient.tokenRefreshToken(
            clientId = client.clientId,
            clientSecret = client.clientSecret,
            refreshToken = tokenResponse.refreshToken
        ).apply {
            expectStatus().isBadRequest
            expectBody().jsonPath("$.error").isEqualTo("invalid_grant")
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
