package me.yapoo.oauth.domain.device.session

import me.yapoo.oauth.domain.client.ClientId
import java.time.Duration
import java.time.Instant

data class DeviceAuthorizationSession(
    val id: DeviceAuthorizationSessionId,
    val clientId: ClientId,
    val scopes: List<String>,
    val deviceCode: String,
    val issuedAt: Instant,
) {

    val expiresIn: Duration = Duration.ofSeconds(600)
}
