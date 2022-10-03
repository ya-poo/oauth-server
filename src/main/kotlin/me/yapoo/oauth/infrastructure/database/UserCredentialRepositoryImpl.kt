package me.yapoo.oauth.infrastructure.database

import com.password4j.Password
import me.yapoo.oauth.domain.user.UserCredential
import me.yapoo.oauth.domain.user.UserCredentialRepository
import me.yapoo.oauth.domain.user.UserSubject
import org.springframework.stereotype.Repository

@Repository
class UserCredentialRepositoryImpl : UserCredentialRepository {

    override fun findByEmail(
        email: String
    ): UserCredential? {
        return if (email == "test@example.com") {
            val password = Password.hash("password")
                .addRandomSalt()
                .withPBKDF2()

            UserCredential(
                id = UserSubject("id"),
                email = "test@example.com",
                passwordHash = password.result,
                salt = password.salt
            )
        } else {
            null
        }
    }
}
