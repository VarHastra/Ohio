package com.github.varhastra.ohio.lexer

enum class TokenType {

    // Single character tokens
    LEFT_PAREN,
    RIGHT_PAREN, SEMICOLON,

    // Multiple character tokens
    COLON_EQUAL,

    // Literals
    IDENTIFIER,

    // Keywords
    NOT,
    AND, NAND, XOR, XNOR, OR, NOR, IMP, NIMP,
    TRUE, FALSE,
    PRINT, PRINTLN,

    EOF
}