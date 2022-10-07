package me.yapoo.oauth.router.authentication.client

import com.password4j.Password
import me.yapoo.oauth.domain.client.ClientId
import org.springframework.stereotype.Repository

@Repository
class ClientCredentialRepositoryImpl : ClientCredentialRepository {

    private val list = mutableListOf(
        run {
            val credential = Password.hash("password")
                .addRandomSalt()
                .withArgon2()
            ClientCredential(
                id = ClientId("sample-client"),
                credentialHash = credential.result,
                salt = credential.salt
            )
        }
    )

    override suspend fun save(
        credential: ClientCredential
    ) {
        list.add(credential)
    }

    override suspend fun find(
        id: ClientId
    ): ClientCredential? {
        return list.singleOrNull { it.id == id }
    }
}
