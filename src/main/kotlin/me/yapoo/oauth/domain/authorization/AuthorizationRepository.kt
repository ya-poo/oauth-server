package me.yapoo.oauth.domain.authorization

interface AuthorizationRepository {

    suspend fun add(authorization: Authorization)

    suspend fun findById(id: AuthorizationId): Authorization?

    suspend fun update(
        authorization: Authorization
    )

    suspend fun delete(id: AuthorizationId)
}
