package me.yapoo.oauth.infrastructure.database

import arrow.core.NonEmptyList
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import org.springframework.stereotype.Repository

@Repository
class ClientRepositoryImpl : ClientRepository {

    private val list = mutableListOf(
        Client(
            id = ClientId("sample-client"),
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
    )

    override suspend fun add(
        client: Client
    ) {
        list.add(client)
    }

    override suspend fun findById(
        id: ClientId
    ): Client? {
        return list.singleOrNull { it.id == id }
    }
}
