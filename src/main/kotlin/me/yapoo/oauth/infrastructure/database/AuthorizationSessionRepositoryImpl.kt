package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.session.AuthorizationSession
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionId
import me.yapoo.oauth.domain.authorization.session.AuthorizationSessionRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AuthorizationSessionRepositoryImpl : AuthorizationSessionRepository {

    private val data = ConcurrentHashMap<AuthorizationSessionId, AuthorizationSession>()

    override fun add(session: AuthorizationSession) {
        data[session.id] = session
    }
}
