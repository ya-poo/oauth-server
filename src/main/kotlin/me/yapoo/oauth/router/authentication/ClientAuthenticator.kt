package me.yapoo.oauth.router.authentication

import arrow.core.continuations.nullable
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientId
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitFormData

@Component
class ClientAuthenticator(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
) {

    // クライアント認証 (RFC 6749 - 2.3)
    // 実装が面倒なので NOT RECOMMENDED なリクエストボディを用いた認証(同 2.3.1)を行う。
    // ブルートフォースアタック対策は省略。
    suspend fun doAuthentication(
        request: ServerRequest
    ): Client? {
        return nullable {
            val body = request.awaitFormData()
            val clientId = body.getSingle("client_id")
                ?.let(::ClientId)
                .bind()

            val clientSecret = body.getSingle("client_secret").bind()

            val clientCredential = clientCredentialRepository.find(clientId)

            ensure(clientCredential.accepts(clientSecret))

            clientRepository.findById(clientId)
        }
    }
}
