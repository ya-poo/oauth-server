package me.yapoo.oauth.router.authentication.client

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

    companion object {
        const val CREDENTIAL_LENGTH = 30

        fun new(
            id: ClientId,
            plainCredential: String,
        ): ClientCredential {
            val credential = Password.hash(plainCredential)
                .addRandomSalt()
                .withArgon2()
            return ClientCredential(id, credential.result, credential.salt)
        }
    }
}
