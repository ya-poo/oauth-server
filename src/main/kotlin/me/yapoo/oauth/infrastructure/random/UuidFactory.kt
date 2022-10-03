package me.yapoo.oauth.infrastructure.random

import org.springframework.stereotype.Component
import java.util.*

@Component
class UuidFactory {

    fun next(): UUID {
        return UUID.randomUUID()
    }
}
