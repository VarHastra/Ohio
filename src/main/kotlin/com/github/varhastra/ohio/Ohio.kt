package com.github.varhastra.ohio

import com.github.varhastra.ohio.interpreter.Interpreter
import com.github.varhastra.ohio.lexer.Lexer
import com.github.varhastra.ohio.lexer.exceptions.LexException
import com.github.varhastra.ohio.lexer.exceptions.UnexpectedSymbolException
import com.github.varhastra.ohio.parser.Parser
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size > 1 -> renderUsagePrompt()
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
    val lexer = Lexer(source)
    val tokens = lexer.lex()
    if (lexer.hasErrors) {
        reportLexerErrors(source, lexer.lexExceptions)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }

    val parser = Parser(tokens)
    val statements = parser.parse()
    if (parser.hasErrors) {
        reportParserErrors(source, parser.parseExceptions)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }

    val interpreter = Interpreter()
    interpreter.interpret(statements)
    if (interpreter.hasErrors) {
        reportRuntimeError(source, interpreter.runtimeFailure!!)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }
}

private fun reportLexerErrors(source: String, unexpectedChars: List<LexException>) {
    println("${unexpectedChars.size} error(s) found:")

    val numOfErrorsToReport = 10
    val numOfUnreportedErrors = unexpectedChars.size - numOfErrorsToReport
    unexpectedChars.take(numOfErrorsToReport).forEach { lexException ->
        reportLexerError(source, lexException)
    }

    if (numOfUnreportedErrors > 0) {
        println("and $numOfUnreportedErrors more...")
    }
}

private fun reportLexerError(source: String, exception: LexException) {
    when (exception) {
        is UnexpectedSymbolException -> report(source, exception)
        else -> throw RuntimeException("Unknown LexerException encountered.")
    }
}

private fun report(source: String, exception: UnexpectedSymbolException) {
    val pos = exception.positionInTheSource
    val errorLine = source.lines()[pos.line]
    println("(${pos.line}:${pos.columnRange.first}) unexpected symbol: '${errorLine.substring(pos.columnRange)}'")
}

fun reportParserErrors(source: String, exceptions: List<Parser.ParseException>) {
    println("${exceptions.size} error(s) found:")

    val numOfErrorsToReport = 10
    val numOfUnreportedErrors = exceptions.size - numOfErrorsToReport
    exceptions.take(numOfErrorsToReport).forEach { exception ->
        reportParseError(source, exception)
    }

    if (numOfUnreportedErrors > 0) {
        println("and $numOfUnreportedErrors more...")
    }
}

fun reportParseError(source: String, exception: Parser.ParseException) {
    val token = exception.token
    val msg = exception.message
    val errorLine = source.lines()[token.line]
    println("(${token.line}:${token.columnRange.first}): $msg")
    println("\t$errorLine")
    print("\t")
    for (i in 0 until token.columnRange.first) {
        print(" ")
    }
    for (i in token.columnRange) {
        print("^")
    }
    println()
}

fun reportRuntimeError(source: String, runtimeFailure: Interpreter.RuntimeFailureException) {
    val token = runtimeFailure.token
    val msg = runtimeFailure.message
    val errorLine = source.lines()[token.line]
    println("(${token.line}:${token.columnRange.first}): $msg")
    println("\t$errorLine")
    print("\t")
    for (i in 0 until token.columnRange.first) {
        print(" ")
    }
    for (i in token.columnRange) {
        print("^")
    }
    println()
}