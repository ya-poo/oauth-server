package me.yapoo.oauth.router.authentication

import com.password4j.Password
import me.yapoo.oauth.domain.client.ClientId

data class ClientCredential(
    val id: ClientId,
    private val credentialHash: String,
    private val salt: String,
) {

    fun accepts(
        plainCredential: String
    ): Boolean {
        return Password
            .check(plainCredential, credentialHash)
            .addSalt(salt)
            .withArgon2()
    }
}
