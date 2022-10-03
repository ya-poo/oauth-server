package me.yapoo.oauth.domain.user

interface UserCredentialRepository {

    fun findByEmail(email: String): UserCredential?
}
