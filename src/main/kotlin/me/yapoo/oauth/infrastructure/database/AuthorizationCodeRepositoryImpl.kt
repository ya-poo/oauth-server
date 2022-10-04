package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.authorization.AuthorizationCode
import me.yapoo.oauth.domain.authorization.AuthorizationCodeRepository
import org.springframework.stereotype.Repository

@Repository
class AuthorizationCodeRepositoryImpl : AuthorizationCodeRepository {

    private val list = mutableListOf<AuthorizationCode>()

    override suspend fun save(
        authorizationCode: AuthorizationCode
    ) {
        list.add(authorizationCode)
    }

    override suspend fun findByCode(
        code: String,
    ): AuthorizationCode? {
        return list.singleOrNull { it.value == code }
    }
}
