package me.yapoo.oauth.log

import org.slf4j.Logger

fun Logger.info(
    error: Throwable? = null,
    message: () -> String,
) {
    if (isInfoEnabled) {
        this.info(message(), error)
    }
}

fun Logger.warn(
    error: Throwable? = null,
    message: () -> String,
) {
    if (isWarnEnabled) {
        warn(message(), error)
    }
}

fun Logger.error(
    error: Throwable? = null,
    message: () -> String,
) {
    if (isErrorEnabled) {
        error(message(), error)
    }
}
