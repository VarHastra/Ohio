package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*
import com.github.varhastra.ohio.translator.Instruction.*
import com.github.varhastra.ohio.translator.Register64.*
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
        if (expr.value is Long) {
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

        pop(RAX)
        processUnaryOperation(expr)

        push(RAX)
    }

    private fun process(expr: Binary) {
        process(expr.left)
        process(expr.right)

        pop(RBX)
        pop(RAX)
        processBinOperation(expr.operation)

        push(RAX)
    }

    private fun processUnaryOperation(expr: Unary) {
        if (expr.operation == TokenType.MINUS) {
            neg(RAX)
        } else {
            log(UnsupportedOperation(expr.operation))
        }
    }

    private fun processBinOperation(tokenType: TokenType) {
        when (tokenType) {
            TokenType.PLUS -> add(RAX, RBX)
            TokenType.MINUS -> sub(RAX, RBX)
            TokenType.STAR -> imul(RAX, RBX)
            TokenType.SLASH -> {
                mov(RDX, 0)
                idiv(RBX)
            }
            TokenType.MOD -> {
                mov(RDX, 0)
                idiv(RBX)
                mov(RAX, RDX)
            }
            else -> log(UnsupportedOperation(tokenType))
        }
    }

    private fun mov(reg: Register64, literal: Long) {
        write(MOV, reg, literal)
    }

    private fun mov(reg1: Register64, reg2: Register64) {
        write(MOV, reg1, reg2)
    }

    private fun push(literal: Long) {
        write(PUSH, literal)
    }

    private fun push(reg: Register64) {
        write(PUSH, reg)
    }

    private fun pop(reg: Register64) {
        write(POP, reg)
    }

    private fun add(reg1: Register64, reg2: Register64) {
        write(ADD, reg1, reg2)
    }

    private fun sub(reg1: Register64, reg2: Register64) {
        write(SUB, reg1, reg2)
    }

    private fun imul(reg1: Register64, reg2: Register64) {
        write(IMUL, reg1, reg2)
    }

    private fun idiv(reg: Register64) {
        write(IDIV, reg)
    }

    private fun neg(reg1: Register64) {
        write(NEG, reg1)
    }

    private fun write(instruction: Instruction, literal: Long) {
        writer.write("${instruction.symbol} $literal\n")
    }

    private fun write(instruction: Instruction, reg: Register64) {
        writer.write("${instruction.symbol} ${reg.symbol}\n")
    }

    private fun write(instruction: Instruction, reg1: Register64, literal: Long) {
        writer.write("${instruction.symbol} ${reg1.symbol}, $literal\n")
    }

    private fun write(instruction: Instruction, reg1: Register64, reg2: Register64) {
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




