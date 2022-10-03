package me.yapoo.oauth.domain.authorization

interface AuthorizationCodeRepository {

    suspend fun save(
        authorizationCode: AuthorizationCode,
    )
}
