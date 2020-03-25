package com.github.varhastra.ohio

import com.github.varhastra.ohio.lexer.Lexer
import com.github.varhastra.ohio.parser.Parser
import com.github.varhastra.ohio.translator.TranslationResult.Failure
import com.github.varhastra.ohio.translator.TranslationResult.Success
import com.github.varhastra.ohio.translator.Translator
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size == 1 -> if (args[0].endsWithAnyOf(".txt", ".oh")) {
            launchFile(args[0])
        } else {
            launch(args[0])
        }
        else -> renderUsagePrompt()
    }
}

private fun renderUsagePrompt() {
    println("Usage:")
    println("\tohio <file_name>")
    println("\tor")
    println("\tohio <expression>")
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
    val expression = parser.parseExpression()
    if (parser.hasErrors) {
        errorReporter.report(parser.parseExceptions)
        exitProcess(ExitCode.DATA_FORMAT_ERROR.value)
    }

    val translator = Translator()
    when (val result = translator.translate(expression)) {
        is Success -> println(String(result.buffer, result.charset))
        is Failure -> result.errors.forEach(::println)
    }
}


private fun String.endsWithAnyOf(vararg suffixes: String) = suffixes.any { this.endsWith(it, true) }