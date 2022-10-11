package me.yapoo.oauth.domain.authorization

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import me.yapoo.oauth.mixin.hash.Hash

data class ProofKey(
    val codeChallenge: String,
    val codeChallengeMethod: Method
) {

    fun accepts(
        codeVerifier: String
    ): Boolean {
        return when (codeChallengeMethod) {
            Method.Plain -> codeVerifier == codeChallenge
            Method.S256 -> Hash.sha256(codeVerifier) == codeChallenge
        }
    }

    enum class Method {
        Plain,
        S256,
        ;

        companion object {
            fun of(method: String): Method? {
                return when (method) {
                    "plain" -> Plain
                    "S256" -> S256
                    else -> null
                }
            }
        }
    }

    companion object {

        fun of(
            challenge: String,
            methodValue: String?
        ): Either<OfException.InvalidMethodValue, ProofKey> {
            return either.eager {
                val method = if (methodValue == null) Method.Plain else Method.of(methodValue)
                ensureNotNull(method) {
                    OfException.InvalidMethodValue()
                }

                ProofKey(challenge, method)
            }
        }

        sealed class OfException : Exception() {
            class InvalidMethodValue : OfException()
        }
    }
}
