package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.RefreshToken
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryImpl : RefreshTokenRepository {

    private val list = mutableListOf<RefreshToken>()

    override suspend fun save(
        refreshToken: RefreshToken
    ) {
        list.add(refreshToken)
    }
}
