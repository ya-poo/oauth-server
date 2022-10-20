package me.yapoo.oauth.handler.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.user.UserSubject
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.time.Instant

// OpenID Connect Core - 2
data class IdToken(
    private val sub: UserSubject,
    private val clientId: ClientId,
    private val now: Instant,
    private val authTime: Instant,
    private val nonce: String?,
    private val rsaPublicKey: RSAPublicKey,
    private val rsaPrivateKey: RSAPrivateKey,
) {

    // TODO: acr, amr をサポート
    val value: String = JWT.create().apply {
        withIssuer("oauth-server")
        withAudience(clientId.value)
        withSubject(sub.value)
        withIssuedAt(now)
        withExpiresAt(now + LIFETIME)
        withClaim("auth_time", authTime)
        if (nonce != null) {
            withClaim("nonce", nonce)
        }
    }.sign(Algorithm.RSA256(rsaPublicKey, rsaPrivateKey))

    companion object {
        private val LIFETIME = Duration.ofMinutes(30)
    }
}
