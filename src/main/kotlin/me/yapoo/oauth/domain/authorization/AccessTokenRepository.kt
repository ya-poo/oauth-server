package me.yapoo.oauth.domain.authorization

interface AccessTokenRepository {

    suspend fun add(accessToken: AccessToken)

    suspend fun findByToken(token: String): AccessToken?

    suspend fun findByAuthorizationId(id: AuthorizationId): AccessToken?

    suspend fun delete(token: String)
}
