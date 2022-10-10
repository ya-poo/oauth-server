package me.yapoo.oauth.domain.client

interface ClientRepository {

    suspend fun add(client: Client)

    suspend fun findById(id: ClientId): Client?
}
