package me.yapoo.oauth.mixin

import java.net.URI

val URI.queryParams: Map<String, List<String>>
    get() = query.split("&").map { pair ->
        val key = pair.substringBefore("=")
        val value = pair.substringAfter("=")
        key to value
    }.groupBy(keySelector = { it.first }, valueTransform = { it.second })
