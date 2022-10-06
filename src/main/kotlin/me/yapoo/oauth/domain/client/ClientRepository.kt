package me.yapoo.oauth.domain.client

interface ClientRepository {

    suspend fun save(client: Client)

    suspend fun findById(id: ClientId): Client?
}
