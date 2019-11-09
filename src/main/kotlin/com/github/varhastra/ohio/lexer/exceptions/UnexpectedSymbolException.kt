package com.github.varhastra.ohio.lexer.exceptions

import com.github.varhastra.ohio.common.Position

class UnexpectedSymbolException(
    positionInTheSource: Position,
    message: String,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : LexException(positionInTheSource, message, cause, enableSuppression, writableStackTrace)