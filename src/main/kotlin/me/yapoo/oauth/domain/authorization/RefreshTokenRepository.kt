package me.yapoo.oauth.domain.authorization

interface RefreshTokenRepository {

    suspend fun add(refreshToken: RefreshToken)

    suspend fun findByToken(
        refreshToken: String
    ): RefreshToken?

    suspend fun delete(
        refreshToken: String
    )

    suspend fun deleteByAuthorizationId(
        id: AuthorizationId
    )
}
