package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.translator.instructionbuffer.InstructionBuffer
import java.io.Closeable
import java.io.OutputStream
import java.nio.charset.Charset

class NasmWriter(destination: OutputStream, charset: Charset = Charsets.UTF_8) : Closeable {

    private val writer = destination.bufferedWriter(charset)

    fun write(buffer: InstructionBuffer) {
        writer.use {
            buffer.forEach { instruction ->
                val indent = if (instruction.startsWith("section")) 0 else 1
                write(instruction, indent)
            }
        }
    }

    fun write(instruction: String, indent: Int = 1, indentSymbol: String = "  ") {
        repeat(indent) { writer.write(indentSymbol) }
        writer.write(instruction)
        writer.write("\n")
    }

    override fun close() {
        writer.close()
    }
}