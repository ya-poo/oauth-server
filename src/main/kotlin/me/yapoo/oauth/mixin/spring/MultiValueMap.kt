package me.yapoo.oauth.mixin.spring

import org.springframework.util.MultiValueMap

fun MultiValueMap<String, String>.getSingle(
    key: String
): String? {
    return this[key]?.singleOrNull()
}
