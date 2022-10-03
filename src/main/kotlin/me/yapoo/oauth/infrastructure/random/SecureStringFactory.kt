package me.yapoo.oauth.infrastructure.random

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class SecureStringFactory {

    fun next(length: Int): String {
        val bytes = ByteArray(length)
        gen.nextBytes(bytes)
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    companion object {
        private val gen: SecureRandom = SecureRandom()
    }
}
