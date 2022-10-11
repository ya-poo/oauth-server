package me.yapoo.oauth.mixin.hash

import java.security.MessageDigest

object Hash {

    private val sha256 = MessageDigest.getInstance("SHA-256")

    fun sha256(value: String): String {
        return sha256
            .digest(value.toByteArray())
            .joinToString(separator = "") {
                "%02x".format(it)
            }
    }
}
