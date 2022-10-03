package me.yapoo.oauth.domain.user

import com.password4j.Password

data class UserCredential(
    val id: UserSubject,
    val email: String,
    private val passwordHash: String,
    private val salt: String,
) {

    fun accepts(
        plainPassword: String
    ): Boolean {
        return Password
            .check(plainPassword, passwordHash)
            .addSalt(salt)
            .withPBKDF2()
    }
}
