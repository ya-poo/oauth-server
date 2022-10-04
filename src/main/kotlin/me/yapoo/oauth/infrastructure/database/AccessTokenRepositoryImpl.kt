package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.AccessToken
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import org.springframework.stereotype.Repository

@Repository
class AccessTokenRepositoryImpl : AccessTokenRepository {

    private val list = mutableListOf<AccessToken>()

    override suspend fun save(
        accessToken: AccessToken
    ) {
        list.add(accessToken)
    }
}
