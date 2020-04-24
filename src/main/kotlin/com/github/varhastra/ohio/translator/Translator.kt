package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*
import com.github.varhastra.ohio.translator.TranslationError.*
import com.github.varhastra.ohio.translator.instructionbuffer.InstructionBuffer
import com.github.varhastra.ohio.translator.instructionbuffer.Register32.*
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class Translator(
    private val charset: Charset = Charsets.UTF_8,
    private val constantFoldingIsOn: Boolean = true,
    private val peepholeOptimizationIsOn: Boolean = true
) {

    private lateinit var outputStream: ByteArrayOutputStream

    private lateinit var instructionBuffer: InstructionBuffer

    private val identifiersCollector = IdentifiersCollector()

    private val errors = mutableListOf<TranslationError>()

    private val hasErrors get() = errors.size != 0

    private val varIdentifiers = mutableSetOf<String>()

    fun translate(expr: Expr): TranslationResult {
        clearLog()

        val expression = if (constantFoldingIsOn) expr.fold() else expr
        gatherVariables(expression)

        outputStream = ByteArrayOutputStream()
        instructionBuffer = InstructionBuffer(peepholeOptimizationIsOn)
        generateBufferContent(expression)
        writeBufferToDestination()

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

    private fun generateBufferContent(expr: Expr) {
        generateTextSection(expr)
        generateRdataSection()
        generateBssSection()
    }

    private fun writeBufferToDestination() {
        NasmWriter(outputStream, charset).use {
            it.write(instructionBuffer)
        }
    }

    private fun generateTextSection(expr: Expr) {
        instructionBuffer.run {
            section(".text")
            globalDef("_main")
            externDef("_printf")
            externDef("_scanf")
            label("_main")
        }

        generateScanCalls()

        process(expr)

        generatePrintfCall()
        instructionBuffer.run {
            ret()
        }
    }

    private fun generateScanCalls() {
        varIdentifiers.forEach { identifier ->
            instructionBuffer.run {
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
        instructionBuffer.run {
            push("message")
            call("_printf")
            pop(EBX)
            pop(EBX)
        }
    }

    private fun generateRdataSection() {
        instructionBuffer.run {
            section(".rdata")
            label("message", "db 'Result is %d', 10, 0")
            label("scanf_format", "db '%d', 0")
        }

        varIdentifiers.forEach { identifier ->
            instructionBuffer.label("$identifier@prompt", "db 'Enter $identifier: ', 0")
        }
    }

    private fun generateBssSection() {
        instructionBuffer.section(".bss")
        varIdentifiers.forEach { identifier ->
            instructionBuffer.label(identifier, "resq 1")
        }
    }

    private fun process(expr: Expr) {
        when (expr) {
            is Literal -> processLiteral(expr)
            is Grouping -> processGrouping(expr)
            is Unary -> processUnary(expr)
            is Binary -> processBinary(expr)
            is Var -> processVar(expr)
            else -> log(UnsupportedExpression(expr))
        }
    }

    private fun processLiteral(expr: Literal) {
        if (expr.value is Int) {
            instructionBuffer.push(expr.value)
        } else {
            log(UnsupportedOperand(expr.value))
        }
    }

    private fun processGrouping(expr: Grouping) {
        process(expr.expr)
    }

    private fun processUnary(expr: Unary) {
        process(expr.right)

        instructionBuffer.pop(EAX)
        processUnaryOperation(expr)

        instructionBuffer.push(EAX)
    }

    private fun processBinary(expr: Binary) {
        process(expr.left)
        process(expr.right)

        instructionBuffer.run {
            pop(EBX)
            pop(EAX)
        }
        processBinOperation(expr.operation)

        instructionBuffer.push(EAX)
    }

    private fun processVar(expr: Var) {
        instructionBuffer.push("dword ${expr.memRef}")
    }

    private fun processUnaryOperation(expr: Unary) {
        if (expr.operation == TokenType.MINUS) {
            instructionBuffer.neg(EAX)
        } else {
            log(UnsupportedOperation(expr.operation))
        }
    }

    private fun processBinOperation(tokenType: TokenType) {
        when (tokenType) {
            TokenType.PLUS -> instructionBuffer.add(EAX, EBX)
            TokenType.MINUS -> instructionBuffer.sub(EAX, EBX)
            TokenType.STAR -> instructionBuffer.imul(EAX, EBX)
            TokenType.SLASH -> {
                instructionBuffer.mov(EDX, 0)
                instructionBuffer.idiv(EBX)
            }
            TokenType.MOD -> {
                instructionBuffer.mov(EDX, 0)
                instructionBuffer.idiv(EBX)
                instructionBuffer.mov(EAX, EDX)
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