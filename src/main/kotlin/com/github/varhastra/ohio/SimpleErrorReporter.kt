package com.github.varhastra.ohio

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.common.exceptions.PositionAwareException

class SimpleErrorReporter(source: String) : ErrorReporter {

    private val sourceLines = source.lines()

    override fun report(position: Position, msg: String) {
        printMessage(position, msg)
        printErrorLine(position)
        printUnderline(position)
    }

    override fun report(exception: PositionAwareException) {
        report(exception.positionInTheSource, exception.message ?: "")
    }

    override fun report(exceptions: List<PositionAwareException>, numOfExceptionsToReport: Int) {
        println("${exceptions.size} error(s) found:")
        exceptions.take(numOfExceptionsToReport).forEach { exception ->
            report(exception)
        }

        val numOfUnreportedExceptions = exceptions.size - numOfExceptionsToReport
        if (numOfUnreportedExceptions > 0) {
            println("and $numOfUnreportedExceptions more...")
        }
    }

    private fun printMessage(position: Position, msg: String) {
        println("(${position.line}:${position.firstColumn}): $msg")
    }

    private fun printErrorLine(position: Position) {
        val errorLine = sourceLines[position.line]
        println("\t$errorLine")
    }

    private fun printUnderline(position: Position) {
        print("\t")
        for (i in 0 until position.firstColumn) {
            print(" ")
        }
        for (i in position.columnRange) {
            print("^")
        }
        println()
    }
}