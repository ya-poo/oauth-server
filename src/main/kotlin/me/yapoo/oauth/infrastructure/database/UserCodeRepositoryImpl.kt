package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.device.session.DeviceAuthorizationSessionId
import me.yapoo.oauth.domain.device.session.UserCode
import me.yapoo.oauth.domain.device.session.UserCodeRepository
import org.springframework.stereotype.Repository

@Repository
class UserCodeRepositoryImpl : UserCodeRepository {

    private val list = mutableListOf<UserCode>()

    override suspend fun add(
        userCode: UserCode
    ) {
        list.add(userCode)
    }

    override suspend fun findByDeviceAuthorizationSessionId(
        deviceAuthorizationSessionId: DeviceAuthorizationSessionId
    ): UserCode? {
        return list.singleOrNull {
            it.deviceAuthorizationSessionId == deviceAuthorizationSessionId
        }
    }
}
