package me.yapoo.oauth.infrastructure.database

import arrow.core.NonEmptyList
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import org.springframework.stereotype.Repository

@Repository
class ClientRepositoryImpl : ClientRepository {

    override suspend fun findById(id: ClientId): Client? {
        return if (id.value != "sample-client") {
            null
        } else {
            Client(
                id = id,
                name = "sample-client",
                scopes = NonEmptyList(
                    head = "user",
                    tail = emptyList()
                ),
                redirectUris = NonEmptyList(
                    head = "http://localhost",
                    tail = emptyList()
                ),
                type = Client.Type.Confidential
            )
        }
    }
}
