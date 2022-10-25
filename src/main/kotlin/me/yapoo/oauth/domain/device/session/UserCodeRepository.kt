package me.yapoo.oauth.domain.device.session

interface UserCodeRepository {

    suspend fun add(
        userCode: UserCode
    )
}
