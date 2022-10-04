package me.yapoo.oauth.domain.authorization

interface RefreshTokenRepository {

    suspend fun save(refreshToken: RefreshToken)
}
