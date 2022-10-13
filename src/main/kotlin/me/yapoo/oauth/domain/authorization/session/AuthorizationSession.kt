package me.yapoo.oauth.domain.authorization.session

import me.yapoo.oauth.domain.authorization.ProofKey
import me.yapoo.oauth.domain.authorization.State
import me.yapoo.oauth.domain.client.ClientId

data class AuthorizationSession(
    val id: AuthorizationSessionId,
    val clientId: ClientId,
    // scope には openid は含めないこととする。
    val scopes: List<String>,
    val state: State?,
    val redirectUri: String,
    val redirectUriSpecified: Boolean,
    val proofKey: ProofKey?,
    val openId: OpenId,
) {

    data class OpenId(
        val required: Boolean,
        val nonce: String?
    )
}
