package me.yapoo.oauth.mixin.arrow

import arrow.core.continuations.EffectScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

suspend fun <R> EffectScope<R>.coEnsure(
    condition: Boolean,
    shift: suspend () -> R
) {
    if (condition) Unit else shift(shift())
}

@OptIn(ExperimentalContracts::class)
suspend fun <R, B : Any> EffectScope<R>.coEnsureNotNull(
    value: B?,
    shift: suspend () -> R
): B {
    contract { returns() implies (value != null) }
    return value ?: shift(shift())
}
