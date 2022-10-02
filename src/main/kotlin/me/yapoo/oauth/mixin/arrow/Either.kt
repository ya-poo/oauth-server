package me.yapoo.oauth.mixin.arrow

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Some

inline fun <A, B> List<A>.rightIfNotEmpty(
    default: () -> B
): Either<B, NonEmptyList<A>> {
    return when (val option = NonEmptyList.fromList(this)) {
        is None -> Either.Left(default())
        is Some -> Either.Right(option.value)
    }
}
