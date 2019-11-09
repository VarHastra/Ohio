package com.github.varhastra.ohio.lexer.exceptions

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.common.exceptions.PositionAwareException

open class LexException(
    positionInTheSource: Position,
    message: String,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : PositionAwareException(positionInTheSource, message, cause, enableSuppression, writableStackTrace)