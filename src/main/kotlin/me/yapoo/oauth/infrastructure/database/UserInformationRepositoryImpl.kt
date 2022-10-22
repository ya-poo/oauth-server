package me.yapoo.oauth.infrastructure.database

import me.yapoo.oauth.domain.user.Gender
import me.yapoo.oauth.domain.user.UserInformation
import me.yapoo.oauth.domain.user.UserInformationRepository
import me.yapoo.oauth.domain.user.UserSubject
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class UserInformationRepositoryImpl : UserInformationRepository {

    private val list = mutableListOf(
        UserInformation(
            id = UserSubject("test-user-id"),
            name = "田中太郎",
            nickname = null,
            preferredUsername = "yapoo",
            email = "test@example.com",
            emailVerified = true,
            gender = Gender.Male,
            birthdate = LocalDate.of(1980, 1, 1),
        )
    )

    override suspend fun findById(
        id: UserSubject
    ): UserInformation? {
        return list.singleOrNull { it.id == id }
    }
}
