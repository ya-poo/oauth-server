package me.yapoo.oauth.domain.authorization.session

import me.yapoo.oauth.infrastructure.random.SecureStringFactory

@JvmInline
value class AuthorizationSessionId(
    val value: String,
) {

    companion object {
        private const val LENGTH = 30

        fun next(
            factory: SecureStringFactory
        ) = AuthorizationSessionId(factory.next(LENGTH))
    }
}
