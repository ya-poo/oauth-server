package me.yapoo.oauth.domain.device.session

data class UserCode(
    val value: String,
    val deviceAuthorizationSessionId: DeviceAuthorizationSessionId,
    val confirmed: Boolean,
) {

    companion object {
        private val CHARSET = 'A'..'Z'
        private const val LENGTH = 8

        fun new(
            deviceAuthorizationSessionId: DeviceAuthorizationSessionId,
        ): UserCode {
            return UserCode(
                value = List(LENGTH) { CHARSET.random() }.joinToString(),
                deviceAuthorizationSessionId = deviceAuthorizationSessionId,
                confirmed = false
            )
        }
    }
}
