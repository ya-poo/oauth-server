package me.yapoo.oauth.domain.authorization.session

interface AuthorizationSessionRepository {

    fun add(session: AuthorizationSession)
}
