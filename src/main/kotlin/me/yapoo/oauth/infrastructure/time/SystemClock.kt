package me.yapoo.oauth.infrastructure.time

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SystemClock {

    fun now(): Instant {
        return Instant.now()
    }
}
