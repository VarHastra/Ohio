package com.github.varhastra.ohio.lexer.exceptions

import com.github.varhastra.ohio.lexer.Position

open class LexException(
    val positionInTheSource: Position,
    message: String,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : RuntimeException(message, cause, enableSuppression, writableStackTrace)