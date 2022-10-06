package me.yapoo.oauth.router.authentication

import me.yapoo.oauth.domain.client.ClientId

interface ClientCredentialRepository {

    suspend fun save(
        credential: ClientCredential
    )

    suspend fun find(
        id: ClientId
    ): ClientCredential?
}
