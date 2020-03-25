package com.github.varhastra.ohio.translator

import java.nio.charset.Charset

sealed class TranslationResult {
    data class Success(val buffer: ByteArray, val charset: Charset) : TranslationResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (!buffer.contentEquals(other.buffer)) return false
            if (charset != other.charset) return false

            return true
        }

        override fun hashCode(): Int {
            var result = buffer.contentHashCode()
            result = 31 * result + charset.hashCode()
            return result
        }
    }

    data class Failure(val errors: List<TranslationError>) : TranslationResult()
}