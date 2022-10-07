package me.yapoo.oauth.router.authentication.client

import me.yapoo.oauth.domain.client.ClientId

interface ClientCredentialRepository {

    suspend fun save(
        credential: ClientCredential
    )

    suspend fun find(
        id: ClientId
    ): ClientCredential?
}
