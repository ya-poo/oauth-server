package me.yapoo.oauth.domain.device.session

interface DeviceAuthorizationSessionRepository {

    suspend fun add(
        deviceAuthorizationSession: DeviceAuthorizationSession
    )

    suspend fun findByDeviceCode(
        deviceCode: String
    ): DeviceAuthorizationSession?
}
