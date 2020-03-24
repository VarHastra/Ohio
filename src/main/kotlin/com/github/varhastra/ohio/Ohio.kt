package com.github.varhastra.ohio

import com.github.varhastra.ohio.interpreter.Interpreter
import com.github.varhastra.ohio.lexer.Lexer
import com.github.varhastra.ohio.parser.Parser
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size == 1 -> launchFile(args[0])
        else -> renderUsagePrompt()
    }
}

private fun renderUsagePrompt() {
    println("Usage: ohio <file_name>")
    exitProcess(ExitCode.USAGE_ERROR.value)
}

private fun launchFile(path: String) {
    val file = File(path)
    if (!file.isFile) {
        println("File at '$path' does not exist or is not a file.")
        exitProcess(ExitCode.CAN_NOT_OPEN_INPUT.value)
    }
    val source = file.readText()
    launch(source)
}

private fun launch(source: String) {
    val errorReporter = SimpleErrorReporter(source)
    val lexer = Lexer(source)
    val tokens = lexer.lex()
    if (lexer.hasErrors) {
        errorReporter.report(lexer.lexExceptions)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }

    val parser = Parser(tokens)
    val statements = parser.parse()
    if (parser.hasErrors) {
        errorReporter.report(parser.parseExceptions)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }

    val interpreter = Interpreter()
    interpreter.interpret(statements)
    if (interpreter.hasErrors) {
        errorReporter.report(interpreter.runtimeFailure!!)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }
}