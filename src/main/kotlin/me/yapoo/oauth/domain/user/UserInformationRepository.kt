package me.yapoo.oauth.domain.user

interface UserInformationRepository {

    suspend fun findById(
        id: UserSubject
    ): UserInformation?
}
