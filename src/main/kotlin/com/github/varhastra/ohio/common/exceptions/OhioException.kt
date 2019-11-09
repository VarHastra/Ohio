package com.github.varhastra.ohio.common.exceptions

open class OhioException(
    message: String,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : RuntimeException(message, cause, enableSuppression, writableStackTrace)