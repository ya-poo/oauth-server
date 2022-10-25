package me.yapoo.oauth.handler.metadata

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Service
class MetadataHandler {

    // OAuth 2.0 Authorization Server Metadata (RFC 8414)
    suspend fun handle(): ServerResponse {
        return ServerResponse.ok()
            .bodyValueAndAwait(MetadataResponse())
    }
}
