package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSession
import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSessionRepository
import org.springframework.stereotype.Repository

@Repository
class DeviceAuthorizationSessionRepositoryImpl : DeviceAuthorizationSessionRepository {

    private val list = mutableListOf<DeviceAuthorizationSession>()

    override suspend fun add(
        deviceAuthorizationSession: DeviceAuthorizationSession
    ) {
        list.add(deviceAuthorizationSession)
    }
}
