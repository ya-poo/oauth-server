package me.yapoo.oauth.router.authentication

import com.password4j.Password
import me.yapoo.oauth.domain.client.ClientId
import org.springframework.stereotype.Repository

@Repository
class ClientCredentialRepository {

    fun find(
        id: ClientId
    ): ClientCredential {
        val credential = Password.hash("password")
            .addRandomSalt()
            .withArgon2()
        return ClientCredential(
            id = id,
            credentialHash = credential.result,
            salt = credential.salt,
        )
    }
}
