package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.session.AuthorizationSession
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import org.springframework.stereotype.Component

@Component
class AuthorizationSessionRepositoryImpl : AuthorizationSessionRepository {

    private val list = mutableListOf<AuthorizationSession>()

    override suspend fun add(session: AuthorizationSession) {
        list.add(session)
    }

    override suspend fun findById(
        id: AuthorizationSessionId
    ): AuthorizationSession? {
        return list.singleOrNull { it.id == id }
    }
}
