package me.yapoo.oauth.domain.client

interface ClientRepository {

    suspend fun findById(id: ClientId): Client?
}
