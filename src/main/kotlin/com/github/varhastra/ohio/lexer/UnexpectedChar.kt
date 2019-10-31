package com.github.varhastra.ohio.lexer

data class UnexpectedChar(
    val char: Char,
    val line: Int,
    val column: Int
)