package com.github.varhastra.ohio.translator.nasmwriter

import com.github.varhastra.ohio.translator.nasmwriter.Instruction.*
import java.io.Closeable
import java.io.Writer

class NasmWriter(private val writer: Writer) : Closeable {

    fun section(name: String) {
        write("section $name")
    }

    fun globalDef(name: String) {
        write("global $name")
    }

    fun externDef(name: String) {
        write("extern $name")
    }

    fun label(name: String, value: String = "") {
        write("$name: $value")
    }

    fun comment(msg: String) {
        write("; $msg")
    }

    fun mov(reg: Register32, literal: Int) {
        write(MOV, reg, literal)
    }

    fun mov(reg1: Register32, reg2: Register32) {
        write(MOV, reg1, reg2)
    }

    fun mov(reg: Register32, label: String) {
        write(MOV, reg, label)
    }

    fun push(label: String) {
        write(PUSH, label)
    }

    fun push(literal: Int) {
        write(PUSH, literal)
    }

    fun push(reg: Register32) {
        write(PUSH, reg)
    }

    fun pop(reg: Register32) {
        write(POP, reg)
    }

    fun add(reg1: Register32, reg2: Register32) {
        write(ADD, reg1, reg2)
    }

    fun sub(reg1: Register32, reg2: Register32) {
        write(SUB, reg1, reg2)
    }

    fun imul(reg1: Register32, reg2: Register32) {
        write(IMUL, reg1, reg2)
    }

    fun idiv(reg: Register32) {
        write(IDIV, reg)
    }

    fun neg(reg1: Register32) {
        write(NEG, reg1)
    }

    fun call(label: String) {
        write("call $label")
    }

    fun ret() {
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

    override fun close() {
        writer.close()
    }
}