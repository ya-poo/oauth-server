package me.yapoo.oauth.handler.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.user.Gender
import me.yapoo.oauth.domain.user.UserInformation
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

// OpenID Connect Core - 2, 5.1
data class IdToken(
    private val userInformation: UserInformation,
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
        withSubject(userInformation.id.value)
        withIssuedAt(now)
        withExpiresAt(now + LIFETIME)
        withClaim("auth_time", authTime)
        if (nonce != null) {
            withClaim("nonce", nonce)
        }
        if (userInformation.name != null) {
            withClaim("name", userInformation.name)
        }
        if (userInformation.nickname != null) {
            withClaim("name", userInformation.nickname)
        }
        if (userInformation.preferredUsername != null) {
            withClaim("preferred_username", userInformation.preferredUsername)
        }
        if (userInformation.email != null) {
            withClaim("email", userInformation.email)
        }
        if (userInformation.emailVerified != null) {
            withClaim("email_verified", userInformation.emailVerified)
        }
        if (userInformation.gender != null) {
            withClaim(
                "gender",
                when (userInformation.gender) {
                    Gender.Male -> "male"
                    Gender.Female -> "female"
                }
            )
        }
        if (userInformation.birthdate != null) {
            withClaim("birthdate", userInformation.birthdate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    }.sign(Algorithm.RSA256(rsaPublicKey, rsaPrivateKey))

    companion object {
        private val LIFETIME = Duration.ofMinutes(30)
    }
}
