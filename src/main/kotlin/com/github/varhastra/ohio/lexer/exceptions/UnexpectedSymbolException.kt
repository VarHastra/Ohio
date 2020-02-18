package com.github.varhastra.ohio.lexer.exceptions

import com.github.varhastra.ohio.common.Position

class UnexpectedSymbolException(
    positionInTheSource: Position,
    message: String
) : LexException(positionInTheSource, message)