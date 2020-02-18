package com.github.varhastra.ohio.lexer.exceptions

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.common.exceptions.PositionAwareException

open class LexException(
    positionInTheSource: Position,
    message: String
) : PositionAwareException(positionInTheSource, message)