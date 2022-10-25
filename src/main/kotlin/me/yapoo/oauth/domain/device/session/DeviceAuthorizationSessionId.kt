package me.yapoo.oauth.domain.device.session

import me.yapoo.oauth.infrastructure.random.SecureStringFactory

@JvmInline
value class DeviceAuthorizationSessionId(
    val value: String,
) {

    companion object {
        private const val LENGTH = 30

        fun next(
            factory: SecureStringFactory
        ) = DeviceAuthorizationSessionId(factory.next(LENGTH))
    }
}
