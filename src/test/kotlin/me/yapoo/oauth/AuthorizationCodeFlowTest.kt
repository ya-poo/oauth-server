@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package me.yapoo.oauth

import com.auth0.jwt.JWT
import me.yapoo.oauth.handler.authentication.AuthenticationRequest
import me.yapoo.oauth.handler.client.RegisterClientRequest
import me.yapoo.oauth.handler.client.RegisterClientResponse
import me.yapoo.oauth.handler.token.TokenAuthorizationCodeResponse
import me.yapoo.oauth.mixin.queryParams
import me.yapoo.oauth.request.authenticate
import me.yapoo.oauth.request.authorization
import me.yapoo.oauth.request.registerClient
import me.yapoo.oauth.request.tokenAuthorizationCode
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
        }.exchange().expectBody<Unit>().returnResult().responseHeaders.location!!
        val asi = authorizationResponseLocation.queryParams["asi"]?.singleOrNull()!!

        val authenticationResponseLocation = webTestClient.post().uri("/authentication")
            .bodyValue(
                AuthenticationRequest(
                    authorizationSessionId = asi,
                    email = "test@example.com",
                    password = "password"
                )
            ).exchange().expectBody<Unit>().returnResult().responseHeaders.location!!
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
            ).exchange().expectBody<TokenAuthorizationCodeResponse>().returnResult().responseBody!!

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
    }

    @Test
    fun `パブリッククライアントの PKCE を利用した認可コードフロー`() {
        val client = webTestClient.registerClient(
            type = "public"
        ).expectBody<RegisterClientResponse>().returnResult().responseBody!!

        val codeVerifier = "test-code-verifier"
        val codeChallenge = "d052c829a86b5fb92ac67730855e560fcda5bbfc22e4603c16c4522ee6da3a95"

        val authorizationResponseLocation = webTestClient.authorization(
            clientId = client.clientId,
            redirectUri = client.redirectUris.first(),
            codeChallenge = codeChallenge,
            codeChallengeMethod = "S256"
        ).expectBody<Unit>().returnResult().responseHeaders.location!!
        val asi = authorizationResponseLocation.queryParams["asi"]?.singleOrNull()!!

        val authenticationResponseLocation = webTestClient
            .authenticate(asi = asi)
            .expectBody<Unit>().returnResult().responseHeaders.location!!
        val code = authenticationResponseLocation.queryParams["code"]?.singleOrNull()!!

        webTestClient.tokenAuthorizationCode(
            clientId = client.clientId,
            clientSecret = client.clientSecret,
            code = code,
            redirectUri = client.redirectUris.first(),
            codeVerifier = codeVerifier
        ).expectBody<TokenAuthorizationCodeResponse>().returnResult().responseBody!!
    }

    @Test
    fun `ID トークンを発行する認可コードフロー`() {
        val client = webTestClient.registerClient(
            scopes = listOf("hello", "openid")
        ).expectBody<RegisterClientResponse>().returnResult().responseBody!!
        val authorizationResponseLocation = webTestClient.authorization(
            clientId = client.clientId,
            redirectUri = client.redirectUris.first(),
            scope = "hello openid",
            nonce = "test-nonce"
        ).expectBody<Unit>().returnResult().responseHeaders.location!!
        val asi = authorizationResponseLocation.queryParams["asi"]?.singleOrNull()!!

        val authenticationResponseLocation = webTestClient.authenticate(asi = asi)
            .expectBody<Unit>().returnResult().responseHeaders.location!!
        val code = authenticationResponseLocation.queryParams["code"]?.singleOrNull()!!

        val tokenResponse = webTestClient.tokenAuthorizationCode(
            clientId = client.clientId,
            clientSecret = client.clientSecret,
            code = code,
            redirectUri = client.redirectUris.first(),
        ).expectBody<TokenAuthorizationCodeResponse>().returnResult().responseBody!!

        val idToken = JWT.decode(tokenResponse.idToken)
        assertEquals("oauth-server", idToken.issuer)
        assertEquals(listOf(client.clientId), idToken.audience)
        assertEquals("test-nonce", idToken.claims["nonce"]?.asString())
        assertEquals("test-user-id", idToken.subject)
        assertEquals("田中太郎", idToken.claims["name"]?.asString())
        assertEquals("yapoo", idToken.claims["preferred_username"]?.asString())
        assertEquals("test@example.com", idToken.claims["email"]?.asString())
        assertEquals(true, idToken.claims["email_verified"]?.asBoolean())
        assertEquals("male", idToken.claims["gender"]?.asString())
        assertEquals("1980-01-01", idToken.claims["birthdate"]?.asString())
    }
}
