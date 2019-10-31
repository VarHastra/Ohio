package com.github.varhastra.ohio.lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int,
    val columnRange: IntRange,
    val sourceRange: IntRange
)