package me.yapoo.oauth.router.authentication.client

import arrow.core.continuations.nullable
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import java.util.*

@Component
class ClientAuthenticator(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
) {

    // クライアント認証 (RFC 6749 - 2.3)
    // Authorization ヘッダによるベーシック認証のみサポートする。
    // リクエストボディを用いた認証(同 2.3.1, NOT RECOMMENDED) はサポートしない。
    // ブルートフォースアタック対策は省略。
    suspend fun doAuthentication(
        request: ServerRequest
    ): Client? {
        return nullable {
            val authorizationHeaderValue = request.headers().header("Authorization")
                .singleOrNull()
                .bind()

            ensure(authorizationHeaderValue.startsWith("Basic "))

            val list = authorizationHeaderValue.substring(6)
                .let { Base64.getDecoder().decode(it).toString() }
                .split(":")

            ensure(list.size == 2)

            val clientId = list[0].let(::ClientId)

            val clientSecret = list[1]

            val clientCredential = clientCredentialRepository.find(clientId).bind()

            ensure(clientCredential.accepts(clientSecret))

            clientRepository.findById(clientId)
        }
    }
}
