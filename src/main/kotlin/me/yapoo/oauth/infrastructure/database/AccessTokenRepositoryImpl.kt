package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.AuthorizationId
import org.springframework.stereotype.Repository

@Repository
class AccessTokenRepositoryImpl : AccessTokenRepository {

    private val list = mutableListOf<AccessToken>()

    override suspend fun add(
        accessToken: AccessToken
    ) {
        list.add(accessToken)
    }

    override suspend fun findByToken(
        token: String
    ): AccessToken? {
        return list.singleOrNull { it.value == token }
    }

    override suspend fun findByAuthorizationId(
        id: AuthorizationId
    ): AccessToken? {
        return list.singleOrNull { it.authorizationId == id }
    }

    override suspend fun delete(
        token: String
    ) {
        list.removeIf { it.value == token }
    }

    override suspend fun deleteByAuthorizationId(
        id: AuthorizationId
    ) {
        list.removeIf { it.authorizationId == id }
    }
}
