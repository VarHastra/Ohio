package com.github.varhastra.ohio.lexer

enum class TokenType {

    // Single character tokens
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    SEMICOLON,
    PLUS, MINUS, STAR, SLASH, MOD,

    // Multiple character tokens
    COLON_EQUAL,
    EQUAL_EQUAL, BANG_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER,
    NUMBER, STRING,

    // Keywords
    NOT,
    AND, NAND, XOR, XNOR, OR, NOR, IMP, NIMP,
    TRUE, FALSE,
    PRINT, PRINTLN,
    IF, ELSE,
    WHILE, REPEAT,

    EOF
}