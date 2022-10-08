package me.yapoo.oauth.domain.authorization

interface AccessTokenRepository {

    suspend fun save(accessToken: AccessToken)

    suspend fun findByToken(token: String): AccessToken?
}
