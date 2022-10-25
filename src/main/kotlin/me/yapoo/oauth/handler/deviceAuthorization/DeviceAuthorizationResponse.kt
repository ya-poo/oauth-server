package me.yapoo.oauth.handler.deviceAuthorization

data class DeviceAuthorizationResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresIn: Int,
) {
    val interval: Int = 5
}
