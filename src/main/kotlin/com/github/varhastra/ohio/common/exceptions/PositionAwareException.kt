package com.github.varhastra.ohio.common.exceptions

import com.github.varhastra.ohio.common.Position

open class PositionAwareException(
    val positionInTheSource: Position,
    message: String,
    cause: Throwable? = null
) : OhioException(message, cause)