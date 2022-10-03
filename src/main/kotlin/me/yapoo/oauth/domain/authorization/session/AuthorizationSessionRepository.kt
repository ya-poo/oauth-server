package me.yapoo.oauth.domain.authorization.session

interface AuthorizationSessionRepository {

    suspend fun add(session: AuthorizationSession)

    suspend fun findById(id: AuthorizationSessionId): AuthorizationSession?
}
