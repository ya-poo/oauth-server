package me.yapoo.oauth.handler.revoke

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.rightIfNotNull
import me.yapoo.oauth.domain.authorization.AccessTokenRepository
import me.yapoo.oauth.domain.authorization.AuthorizationRepository
import me.yapoo.oauth.domain.authorization.RefreshTokenRepository
import me.yapoo.oauth.domain.client.Client
import me.yapoo.oauth.domain.client.ClientRepository
import me.yapoo.oauth.mixin.arrow.coEnsure
import me.yapoo.oauth.mixin.spring.getSingle
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Service
class TokenRevocationHandler(
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clientRepository: ClientRepository,
    private val authorizationRepository: AuthorizationRepository,
) {

    // トークンリボケーションエンドポイント (RFC 7009)
    suspend fun handle(
        request: ServerRequest,
        client: Client?,
    ): Either<ServerResponse, ServerResponse> {
        return either {
            val body = request.awaitFormData()

            val token = body.getSingle("token")
                .rightIfNotNull {
                    ServerResponse.badRequest()
                        .bodyValueAndAwait(
                            TokenRevocationErrorResponse(
                                error = TokenRevocationErrorResponse.ErrorCode.InvalidRequest,
                                errorDescription = "token must be specified"
                            )
                        )
                }.bind()
            // token_type_hint (RFC 7009 - 2.1) は実装が面倒なので一旦サポートしない。

            val accessToken = accessTokenRepository.findByToken(token)
            val refreshToken = if (accessToken == null) refreshTokenRepository.findByToken(token) else null
            coEnsure(accessToken != null || refreshToken != null) {
                ServerResponse.ok().buildAndAwait()
            }

            val authorization = authorizationRepository.findById(
                accessToken?.authorizationId ?: refreshToken!!.authorizationId
            ).rightIfNotNull {
                // トークンはあるが対応する認可がない場合、トークン自体が無効なものとして取り扱っておく
                ServerResponse.ok().buildAndAwait()
            }.bind()

            val tokenClient = clientRepository.findById(authorization.clientId)
                .rightIfNotNull {
                    // トークンはあるが対応するクライアントがない場合、トークン自体が無効なものとして取り扱っておく
                    ServerResponse.ok().buildAndAwait()
                }.bind()

            coEnsure(
                tokenClient.type == Client.Type.Public ||
                    (client != null && authorization.clientId == client.id)
            ) {
                // RFC 7009 - 2.1
                // この場合にはエラーレスポンスを返すことになっているが、
                // こうすると他のクライアントに向けて発行されたトークンであることを識別出来てしまうのでは？
                ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .bodyValueAndAwait(
                        TokenRevocationErrorResponse(
                            error = TokenRevocationErrorResponse.ErrorCode.InvalidClient,
                            errorDescription = "invalid client"
                        )
                    )
            }

            // RFC 7009 によれば、refresh_token を取り消す場合は access_token も取り消すべき (SHOULD) で、
            // access_token を取り消す場合は refresh_token も取り消しても良い (MAY)
            // ここでは両方とも取り消しておくことにする。
            authorizationRepository.delete(authorization.id)
            accessTokenRepository.deleteByAuthorizationId(authorization.id)
            refreshTokenRepository.deleteByAuthorizationId(authorization.id)

            ServerResponse.ok().buildAndAwait()
        }
    }
}
