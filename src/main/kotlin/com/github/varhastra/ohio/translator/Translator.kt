package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*
import com.github.varhastra.ohio.translator.Instruction.*
import com.github.varhastra.ohio.translator.Register32.*
import com.github.varhastra.ohio.translator.TranslationError.*
import java.io.ByteArrayOutputStream
import java.io.Writer
import java.nio.charset.Charset

class Translator(private val charset: Charset = Charsets.UTF_8) {

    private lateinit var outputStream: ByteArrayOutputStream

    private lateinit var writer: Writer

    private val identifiersCollector = IdentifiersCollector()

    private val errors = mutableListOf<TranslationError>()

    private val hasErrors get() = errors.size != 0

    private val varIdentifiers = mutableSetOf<String>()

    fun translate(expr: Expr): TranslationResult {
        clearLog()
        gatherVariables(expr)

        outputStream = ByteArrayOutputStream()
        writer = outputStream.bufferedWriter(charset)
        writer.use {
            generateOutput(expr)
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
        ret()
    }

    private fun generateTextSection(expr: Expr) {
        section(".text")
        globalDef("_main")
        externDef("_printf")
        externDef("_scanf")
        label("_main")

        generateScanCalls()

        process(expr)

        generatePrintfCall()
    }

    private fun generateScanCalls() {
        varIdentifiers.forEach { identifier ->
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

    private fun generatePrintfCall() {
        push("message")
        call("_printf")
        pop(EBX)
        pop(EBX)
    }

    private fun generateRdataSection() {
        section(".rdata")
        label("message", "db 'Result is %d', 10, 0")
        label("scanf_format", "db '%d', 0")

        varIdentifiers.forEach { identifier ->
            label("$identifier@prompt", "db 'Enter $identifier: ', 0")
        }
    }

    private fun generateBssSection() {
        section(".bss")
        varIdentifiers.forEach { identifier ->
            label(identifier, "resq 1")
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
            push(expr.value)
        } else {
            log(UnsupportedOperand(expr.value))
        }
    }

    private fun process(expr: Grouping) {
        process(expr.expr)
    }

    private fun process(expr: Unary) {
        process(expr.right)

        pop(EAX)
        processUnaryOperation(expr)

        push(EAX)
    }

    private fun process(expr: Binary) {
        process(expr.left)
        process(expr.right)

        pop(EBX)
        pop(EAX)
        processBinOperation(expr.operation)

        push(EAX)
    }

    private fun process(expr: Var) {
        push("dword ${expr.memRef}")
    }

    private fun processUnaryOperation(expr: Unary) {
        if (expr.operation == TokenType.MINUS) {
            neg(EAX)
        } else {
            log(UnsupportedOperation(expr.operation))
        }
    }

    private fun processBinOperation(tokenType: TokenType) {
        when (tokenType) {
            TokenType.PLUS -> add(EAX, EBX)
            TokenType.MINUS -> sub(EAX, EBX)
            TokenType.STAR -> imul(EAX, EBX)
            TokenType.SLASH -> {
                mov(EDX, 0)
                idiv(EBX)
            }
            TokenType.MOD -> {
                mov(EDX, 0)
                idiv(EBX)
                mov(EAX, EDX)
            }
            else -> log(UnsupportedOperation(tokenType))
        }
    }

    private fun section(name: String) {
        write("section $name")
    }

    private fun globalDef(name: String) {
        write("global $name")
    }

    private fun externDef(name: String) {
        write("extern $name")
    }

    private fun label(name: String, value: String = "") {
        write("$name: $value")
    }

    private fun comment(msg: String) {
        write("; $msg")
    }

    private fun mov(reg: Register32, literal: Int) {
        write(MOV, reg, literal)
    }

    private fun mov(reg1: Register32, reg2: Register32) {
        write(MOV, reg1, reg2)
    }

    private fun mov(reg: Register32, label: String) {
        write(MOV, reg, label)
    }

    private fun push(label: String) {
        write(PUSH, label)
    }

    private fun push(literal: Int) {
        write(PUSH, literal)
    }

    private fun push(reg: Register32) {
        write(PUSH, reg)
    }

    private fun pop(reg: Register32) {
        write(POP, reg)
    }

    private fun add(reg1: Register32, reg2: Register32) {
        write(ADD, reg1, reg2)
    }

    private fun sub(reg1: Register32, reg2: Register32) {
        write(SUB, reg1, reg2)
    }

    private fun imul(reg1: Register32, reg2: Register32) {
        write(IMUL, reg1, reg2)
    }

    private fun idiv(reg: Register32) {
        write(IDIV, reg)
    }

    private fun neg(reg1: Register32) {
        write(NEG, reg1)
    }

    private fun call(label: String) {
        write("call $label")
    }

    private fun ret() {
        write("ret")
    }

    private fun write(instruction: Instruction, literal: Int) {
        write("${instruction.symbol} $literal")
    }

    private fun write(instruction: Instruction, reg: Register32) {
        write("${instruction.symbol} ${reg.symbol}")
    }

    private fun write(instruction: Instruction, label: String) {
        write("${instruction.symbol} $label")
    }

    private fun write(instruction: Instruction, reg1: Register32, literal: Int) {
        write("${instruction.symbol} ${reg1.symbol}, $literal")
    }

    private fun write(instruction: Instruction, reg1: Register32, label: String) {
        write("${instruction.symbol} ${reg1.symbol}, $label")
    }

    private fun write(instruction: Instruction, reg1: Register32, reg2: Register32) {
        write("${instruction.symbol} ${reg1.symbol}, ${reg2.symbol}")
    }

    private fun write(str: String, indent: Int = 0, indentSymbol: String = "  ") {
        repeat(indent) { writer.write(indentSymbol) }
        writer.write(str)
        writer.write("\n")
    }

    private fun log(error: TranslationError) {
        errors.add(error)
    }

    private fun clearLog() {
        errors.clear()
    }

    private fun clearGlobalIdentifiers() {
        varIdentifiers.clear()
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
        get() = "[${this.identifier.lexeme}]"
}




