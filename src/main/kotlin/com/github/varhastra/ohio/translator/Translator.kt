package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*
import com.github.varhastra.ohio.translator.TranslationError.*
import com.github.varhastra.ohio.translator.nasmwriter.NasmWriter
import com.github.varhastra.ohio.translator.nasmwriter.Register32.*
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class Translator(
    private val charset: Charset = Charsets.UTF_8,
    private val foldConstants: Boolean = true
) {

    private lateinit var outputStream: ByteArrayOutputStream

    private lateinit var writer: NasmWriter

    private val identifiersCollector = IdentifiersCollector()

    private val errors = mutableListOf<TranslationError>()

    private val hasErrors get() = errors.size != 0

    private val varIdentifiers = mutableSetOf<String>()

    fun translate(expr: Expr): TranslationResult {
        clearLog()

        val expression = if (foldConstants) expr.fold() else expr
        gatherVariables(expression)

        outputStream = ByteArrayOutputStream()
        writer = NasmWriter(outputStream.bufferedWriter(charset))
        writer.use {
            generateOutput(expression)
        }

        return if (hasErrors) {
            TranslationResult.Failure(errors.toList())
        } else {
            TranslationResult.Success(outputStream.toByteArray(), charset)
        }
    }

    private fun gatherVariables(expr: Expr) {
        varIdentifiers.clear()
        val identifiers = identifiersCollector.collectIdentifiers(expr)
        varIdentifiers.addAll(identifiers)
    }

    private fun generateOutput(expr: Expr) {
        generateTextSection(expr)
        generateRdataSection()
        generateBssSection()
    }

    private fun generateTextSection(expr: Expr) {
        writer.run {
            section(".text")
            globalDef("_main")
            externDef("_printf")
            externDef("_scanf")
            label("_main")
        }

        generateScanCalls()

        process(expr)

        generatePrintfCall()
        writer.run {
            ret()
        }
    }

    private fun generateScanCalls() {
        varIdentifiers.forEach { identifier ->
            writer.run {
                push("$identifier@prompt")
                call("_printf")
                pop(EBX)
                push(identifier)
                push("scanf_format")
                call("_scanf")
                pop(EBX)
                pop(EBX)
            }
        }
    }

    private fun generatePrintfCall() {
        writer.run {
            push("message")
            call("_printf")
            pop(EBX)
            pop(EBX)
        }
    }

    private fun generateRdataSection() {
        writer.run {
            section(".rdata")
            label("message", "db 'Result is %d', 10, 0")
            label("scanf_format", "db '%d', 0")
        }

        varIdentifiers.forEach { identifier ->
            writer.label("$identifier@prompt", "db 'Enter $identifier: ', 0")
        }
    }

    private fun generateBssSection() {
        writer.section(".bss")
        varIdentifiers.forEach { identifier ->
            writer.label(identifier, "resq 1")
        }
    }

    private fun process(expr: Expr) {
        when (expr) {
            is Literal -> process(expr)
            is Grouping -> process(expr)
            is Unary -> process(expr)
            is Binary -> process(expr)
            is Var -> process(expr)
            else -> log(UnsupportedExpression(expr))
        }
    }

    private fun process(expr: Literal) {
        if (expr.value is Int) {
            writer.push(expr.value)
        } else {
            log(UnsupportedOperand(expr.value))
        }
    }

    private fun process(expr: Grouping) {
        process(expr.expr)
    }

    private fun process(expr: Unary) {
        process(expr.right)

        writer.pop(EAX)
        processUnaryOperation(expr)

        writer.push(EAX)
    }

    private fun process(expr: Binary) {
        process(expr.left)
        process(expr.right)

        writer.run {
            pop(EBX)
            pop(EAX)
        }
        processBinOperation(expr.operation)

        writer.push(EAX)
    }

    private fun process(expr: Var) {
        writer.push("dword ${expr.memRef}")
    }

    private fun processUnaryOperation(expr: Unary) {
        if (expr.operation == TokenType.MINUS) {
            writer.neg(EAX)
        } else {
            log(UnsupportedOperation(expr.operation))
        }
    }

    private fun processBinOperation(tokenType: TokenType) {
        when (tokenType) {
            TokenType.PLUS -> writer.add(EAX, EBX)
            TokenType.MINUS -> writer.sub(EAX, EBX)
            TokenType.STAR -> writer.imul(EAX, EBX)
            TokenType.SLASH -> {
                writer.mov(EDX, 0)
                writer.idiv(EBX)
            }
            TokenType.MOD -> {
                writer.mov(EDX, 0)
                writer.idiv(EBX)
                writer.mov(EAX, EDX)
            }
            else -> log(UnsupportedOperation(tokenType))
        }
    }

    private fun log(error: TranslationError) {
        errors.add(error)
    }

    private fun clearLog() {
        errors.clear()
    }
}


@Suppress("RemoveRedundantQualifierName")
private val Expr.Binary.operation
    get() = this.operator.type

@Suppress("RemoveRedundantQualifierName")
private val Expr.Unary.operation
    get() = this.operator.type

@Suppress("RemoveRedundantQualifierName")
private val Expr.Var.name
    get() = this.identifier.lexeme

@Suppress("RemoveRedundantQualifierName")
private val Expr.Var.memRef
    get() = "[${this.name}]"