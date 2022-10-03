package me.yapoo.oauth.domain.authorization

import me.yapoo.oauth.infrastructure.random.UuidFactory

@JvmInline
value class AuthorizationId(
    val value: String
) {

    companion object {
        fun new(
            uuidFactory: UuidFactory,
        ) = AuthorizationId(uuidFactory.next().toString())
    }
}
