package com.github.varhastra.ohio

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.common.exceptions.PositionAwareException

interface ErrorReporter {

    fun report(position: Position, msg: String)

    fun report(exception: PositionAwareException)

    fun report(exceptions: List<PositionAwareException>, numOfExceptionsToReport: Int = 10)
}