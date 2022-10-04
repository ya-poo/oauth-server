package me.yapoo.oauth.domain.authorization

interface AuthorizationRepository {

    suspend fun save(authorization: Authorization)

    suspend fun findById(id: AuthorizationId): Authorization?
}
