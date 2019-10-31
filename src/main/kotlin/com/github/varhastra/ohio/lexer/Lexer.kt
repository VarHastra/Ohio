package com.github.varhastra.ohio.lexer

import com.github.varhastra.ohio.lexer.TokenType.*

class Lexer(private val source: String) {

    private val tokens = mutableListOf<Token>()
    private val _unexpectedChars = mutableListOf<UnexpectedChar>()
    val unexpectedChars: List<UnexpectedChar>
        get() = _unexpectedChars
    private var current = 0
    private var line = 0
    private var numOfCharsBeforeCurrentLine = 0

    val hasErrors
        get() = _unexpectedChars.size > 0

    fun lex(): List<Token> {
        while (isNotAtEnd()) {
            scanToken()
        }
        addEofToken()

        return tokens
    }

    private fun scanToken() {
        if (matchToken()) return
        if (matchNewline()) return
        if (matchSpace()) return

        error()
    }

    private fun matchToken(): Boolean {
        for (tokenPattern in tokenPatterns) {
            val (pattern, type) = tokenPattern
            val matcher = pattern.matcher(source).region(current, source.length)

            if (matcher.lookingAt()) {
                val lexeme = matcher.group()
                if (type == IDENTIFIER && lexeme in keywords) {
                    addToken(keywords.getValue(lexeme), matcher.start() until matcher.end())
                } else {
                    addToken(type, matcher.start() until matcher.end())
                }
                current = matcher.end()
                return true
            }
        }
        return false
    }

    private fun matchNewline(): Boolean {
        val matcher = newlinePattern.matcher(source).region(current, source.length)

        if (matcher.lookingAt()) {
            line++
            current = matcher.end()
            numOfCharsBeforeCurrentLine = current
            return true
        }
        return false
    }

    private fun matchSpace(): Boolean {
        val matcher = spacePattern.matcher(source).region(current, source.length)

        if (matcher.lookingAt()) {
            current = matcher.end()
            return true
        }
        return false
    }

    private fun error() {
        _unexpectedChars.add(UnexpectedChar(source[current], line, current - numOfCharsBeforeCurrentLine))
        current++
    }

    private fun addToken(type: TokenType, range: IntRange, literal: Any? = null) {
        val lexeme = source.substring(range)
        val columnRange = (range.first - numOfCharsBeforeCurrentLine)..(range.last - numOfCharsBeforeCurrentLine)
        tokens.add(Token(type, lexeme, literal, line, columnRange, range))
    }

    private fun addEofToken() {
        tokens.add(Token(EOF, "", null, line, IntRange.EMPTY, IntRange.EMPTY))
    }

    private fun isAtEnd() = current >= source.length

    private fun isNotAtEnd() = !isAtEnd()


    companion object {
        private val tokenPatterns = mapOf(
            "[(]".toPattern() to LEFT_PAREN,
            "[)]".toPattern() to RIGHT_PAREN,
            ";".toPattern() to SEMICOLON,
            ":=".toPattern() to COLON_EQUAL,
            "[_a-zA-Z][_a-zA-Z0-9]*".toPattern() to IDENTIFIER,
            "1".toPattern() to TRUE,
            "0".toPattern() to FALSE
        )

        private val keywords = mapOf(
            "print" to PRINT,
            "println" to PRINTLN,
            "and" to AND,
            "nand" to NAND,
            "xor" to XOR,
            "xnor" to XNOR,
            "or" to OR,
            "nor" to NOR,
            "imp" to IMP,
            "nimp" to NIMP,
            "not" to NOT
        )

        private val newlinePattern = "\n".toPattern()

        private val spacePattern = "[ \t\r]+".toPattern()
    }
}