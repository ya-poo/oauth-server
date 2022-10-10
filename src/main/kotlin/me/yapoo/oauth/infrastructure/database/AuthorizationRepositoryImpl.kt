package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.Authorization
import me.yapoo.oauth.domain.authorization.AuthorizationId
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import org.springframework.stereotype.Repository

@Repository
class AuthorizationRepositoryImpl : AuthorizationRepository {

    private val list = mutableListOf<Authorization>()

    override suspend fun add(
        authorization: Authorization
    ) {
        list.add(authorization)
    }

    override suspend fun findById(
        id: AuthorizationId
    ): Authorization? {
        return list.singleOrNull { it.id == id }
    }

    override suspend fun update(
        authorization: Authorization
    ) {
        list.removeIf { it.id == authorization.id }
        list.add(authorization)
    }

}
