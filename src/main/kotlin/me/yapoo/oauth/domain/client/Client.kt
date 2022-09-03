package me.yapoo.oauth.domain.client

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.either

data class Client(
    val id: ClientId,
    val name: String,
    val redirectUris: NonEmptyList<String>,
) {

    fun validateRedirectUri(
        uri: String?
    ): Either<ValidateRedirectUriException, String> {
        return either.eager {
            if (uri == null) {
                ensure(redirectUris.size == 1) {
                    ValidateRedirectUriException.CouldNotDetermine()
                }
                redirectUris.head
            } else {
                ensure(redirectUris.contains(uri)) {
                    ValidateRedirectUriException.NotFound()
                }
                uri
            }
        }
    }

    sealed class ValidateRedirectUriException : Exception() {
        class CouldNotDetermine : ValidateRedirectUriException()
        class NotFound : ValidateRedirectUriException()
    }
}
