package com.github.varhastra.ohio.common.exceptions

import com.github.varhastra.ohio.common.Position

open class PositionAwareException(
    val positionInTheSource: Position,
    message: String,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : OhioException(message, cause, enableSuppression, writableStackTrace)