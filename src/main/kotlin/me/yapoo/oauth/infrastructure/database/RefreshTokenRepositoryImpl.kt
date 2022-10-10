package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.RefreshToken
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryImpl : RefreshTokenRepository {

    private val list = mutableListOf<RefreshToken>()

    override suspend fun add(
        refreshToken: RefreshToken
    ) {
        list.add(refreshToken)
    }

    override suspend fun findByToken(
        refreshToken: String
    ): RefreshToken? {
        return list.singleOrNull { it.value == refreshToken }
    }

    override suspend fun delete(
        refreshToken: String
    ) {
        list.removeIf { it.value == refreshToken }
    }
}
