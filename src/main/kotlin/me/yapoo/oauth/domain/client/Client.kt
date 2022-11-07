package me.yapoo.oauth.domain.client

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.either

data class Client(
    val id: ClientId,
    val name: String,
    val scopes: NonEmptyList<String>,
    val redirectUris: NonEmptyList<String>,
    val type: Type,
) {
    // RFC 6749 - 2.1
    enum class Type {
        Confidential,
        Public,
    }

    /*
     * TODO: 下記の考慮が漏れている
     *  1. OpenID Connect では `uri` は指定が必須かつ、事前登録済みのものと完全一致が必要。
     *  2. OAuth 2.0 では認可コードフローかつコンフィデンシャルクライアントの場合、`redirect_uri` は事前登録不要で、
     *     かつその場合は絶対 URI かつフラグメント部を含まない任意の URI を受け付ける。
     */
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
