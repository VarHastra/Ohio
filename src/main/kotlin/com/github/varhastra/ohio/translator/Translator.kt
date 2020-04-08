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

    private val errors = mutableListOf<TranslationError>()

    private val hasErrors get() = errors.size != 0

    fun translate(expr: Expr): TranslationResult {
        clearLog()

        outputStream = ByteArrayOutputStream()
        writer = outputStream.bufferedWriter(charset)
        writer.use {
            process(expr)
        }

        return if (hasErrors) {
            TranslationResult.Failure(errors.toList())
        } else {
            TranslationResult.Success(outputStream.toByteArray(), charset)
        }
    }

    private fun process(expr: Expr) {
        when (expr) {
            is Literal -> process(expr)
            is Unary -> process(expr)
            is Binary -> process(expr)
            is Grouping -> process(expr)
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

    private fun mov(reg: Register32, literal: Int) {
        write(MOV, reg, literal)
    }

    private fun mov(reg1: Register32, reg2: Register32) {
        write(MOV, reg1, reg2)
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

    private fun write(instruction: Instruction, literal: Int) {
        writer.write("${instruction.symbol} $literal\n")
    }

    private fun write(instruction: Instruction, reg: Register32) {
        writer.write("${instruction.symbol} ${reg.symbol}\n")
    }

    private fun write(instruction: Instruction, reg1: Register32, literal: Int) {
        writer.write("${instruction.symbol} ${reg1.symbol}, $literal\n")
    }

    private fun write(instruction: Instruction, reg1: Register32, reg2: Register32) {
        writer.write("${instruction.symbol} ${reg1.symbol}, ${reg2.symbol}\n")
    }

    private fun log(error: TranslationError) {
        errors.add(error)
    }

    private fun clearLog() {
        errors.clear()
    }

    @Suppress("RemoveRedundantQualifierName")
    private val Expr.Binary.operation
        get() = this.operator.type

    @Suppress("RemoveRedundantQualifierName")
    private val Expr.Unary.operation
        get() = this.operator.type
}




