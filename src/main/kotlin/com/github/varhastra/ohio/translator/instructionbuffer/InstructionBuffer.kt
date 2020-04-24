package com.github.varhastra.ohio.translator.instructionbuffer

import com.github.varhastra.ohio.translator.instructionbuffer.Mnemonic.*

class InstructionBuffer(private val peepholeOptimizationIsOn: Boolean) : Iterable<String> {

    private val buffer = mutableListOf<String>()

    val bufferSize
        get() = buffer.size

    private val bufferEndsWithRedundantPush: Boolean
        get() {
            return buffer.last() == "${POP.symbol} ${Register32.EBX.symbol}" && buffer.penult().startsWith(PUSH.symbol)
        }

    fun section(name: String) {
        writeToBuffer("section $name")
    }

    fun globalDef(name: String) {
        writeToBuffer("global $name")
    }

    fun externDef(name: String) {
        writeToBuffer("extern $name")
    }

    fun label(name: String, value: String = "") {
        writeToBuffer("$name: $value")
    }

    fun comment(msg: String) {
        writeToBuffer("; $msg")
    }

    fun mov(reg: Register32, literal: Int) {
        writeToBuffer(MOV, reg, literal)
    }

    fun mov(reg1: Register32, reg2: Register32) {
        writeToBuffer(MOV, reg1, reg2)
    }

    fun mov(reg: Register32, label: String) {
        writeToBuffer(MOV, reg, label)
    }

    fun push(label: String) {
        writeToBuffer(PUSH, label)
    }

    fun push(literal: Int) {
        writeToBuffer(PUSH, literal)
    }

    fun push(reg: Register32) {
        writeToBuffer(PUSH, reg)
    }

    fun pop(reg: Register32) {
        writeToBuffer(POP, reg)
    }

    fun add(reg1: Register32, reg2: Register32) {
        writeToBuffer(ADD, reg1, reg2)
    }

    fun sub(reg1: Register32, reg2: Register32) {
        writeToBuffer(SUB, reg1, reg2)
    }

    fun imul(reg1: Register32, reg2: Register32) {
        writeToBuffer(IMUL, reg1, reg2)
    }

    fun idiv(reg: Register32) {
        writeToBuffer(IDIV, reg)
    }

    fun neg(reg1: Register32) {
        writeToBuffer(NEG, reg1)
    }

    fun call(label: String) {
        writeToBuffer("call $label")
    }

    fun ret() {
        writeToBuffer("ret")
    }

    override fun iterator(): Iterator<String> {
        return buffer.toList().iterator()
    }

    private fun writeToBuffer(mnemonic: Mnemonic, literal: Int) {
        writeToBuffer("${mnemonic.symbol} $literal")
    }

    private fun writeToBuffer(mnemonic: Mnemonic, reg: Register32) {
        writeToBuffer("${mnemonic.symbol} ${reg.symbol}")
    }

    private fun writeToBuffer(mnemonic: Mnemonic, label: String) {
        writeToBuffer("${mnemonic.symbol} $label")
    }

    private fun writeToBuffer(mnemonic: Mnemonic, reg1: Register32, literal: Int) {
        writeToBuffer("${mnemonic.symbol} ${reg1.symbol}, $literal")
    }

    private fun writeToBuffer(mnemonic: Mnemonic, reg1: Register32, label: String) {
        writeToBuffer("${mnemonic.symbol} ${reg1.symbol}, $label")
    }

    private fun writeToBuffer(mnemonic: Mnemonic, reg1: Register32, reg2: Register32) {
        writeToBuffer("${mnemonic.symbol} ${reg1.symbol}, ${reg2.symbol}")
    }

    private fun writeToBuffer(instruction: String) {
        buffer.add(instruction)
        if (peepholeOptimizationIsOn) {
            performPeepholeOptimization()
        }
    }

    private fun performPeepholeOptimization() {
        if (bufferSize < peepholeSize) {
            return
        }

        if (bufferEndsWithRedundantPush) {
            val penult = buffer.penult()
            val substitute = penult.replace(pushPattern, "${MOV.symbol} ${Register32.EBX.symbol}, ")
            buffer.removeAt(buffer.lastIndex)
            buffer[buffer.lastIndex] = substitute
        }
    }
}


private const val peepholeSize = 2

private val pushPattern = Regex("${PUSH.symbol}\\s+")


private fun <T> List<T>.penult(): T {
    if (size < 2) throw NoSuchElementException("List contains less than 2 items.")
    return this[lastIndex - 1]
}