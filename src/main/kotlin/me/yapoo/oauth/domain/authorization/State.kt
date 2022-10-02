package me.yapoo.oauth.domain.authorization

import arrow.core.Either
import arrow.core.continuations.either

@JvmInline
value class State private constructor(
    val value: String
) {

    companion object {
        private val format = Regex("^\\p{ASCII}*")

        fun of(value: String): Either<OfException, State> {
            return either.eager {
                ensure(format.matches(value)) {
                    OfException.Malformed()
                }

                State(value)
            }
        }

        sealed class OfException : Exception() {
            class Malformed : OfException()
        }
    }
}
