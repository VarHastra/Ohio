package com.github.varhastra.ohio.lexer

import com.github.varhastra.ohio.lexer.TokenType.*

class Lexer(private val source: String) {

    val unexpectedChars: List<UnexpectedChar>
        get() = _unexpectedChars
    private val _unexpectedChars = mutableListOf<UnexpectedChar>()

    val hasErrors
        get() = _unexpectedChars.size > 0

    private val tokens = mutableListOf<Token>()

    private var currentPos = 0
    private var line = 0
    private var numOfCharsBeforeCurrentLine = 0

    private val isAtEnd
        get() = currentPos >= source.length

    private val isNotAtEnd
        get() = !isAtEnd


    fun lex(): List<Token> {
        while (isNotAtEnd) {
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
            val matcher = pattern.matcher(source).region(currentPos, source.length)

            if (matcher.lookingAt()) {
                val lexeme = matcher.group()
                if (type == IDENTIFIER && lexeme in keywords) {
                    addToken(keywords.getValue(lexeme), matcher.start() until matcher.end())
                } else {
                    addToken(type, matcher.start() until matcher.end())
                }
                currentPos = matcher.end()
                return true
            }
        }
        return false
    }

    private fun matchNewline(): Boolean {
        val matcher = newlinePattern.matcher(source).region(currentPos, source.length)

        if (matcher.lookingAt()) {
            line++
            currentPos = matcher.end()
            numOfCharsBeforeCurrentLine = currentPos
            return true
        }
        return false
    }

    private fun matchSpace(): Boolean {
        val matcher = spacePattern.matcher(source).region(currentPos, source.length)

        if (matcher.lookingAt()) {
            currentPos = matcher.end()
            return true
        }
        return false
    }

    private fun error() {
        _unexpectedChars.add(UnexpectedChar(source[currentPos], line, currentPos - numOfCharsBeforeCurrentLine))
        currentPos++
    }

    private fun addToken(type: TokenType, range: IntRange, literal: Any? = null) {
        val lexeme = source.substring(range)
        val columnRange = (range.first - numOfCharsBeforeCurrentLine)..(range.last - numOfCharsBeforeCurrentLine)
        tokens.add(Token(type, lexeme, literal, line, columnRange, range))
    }

    private fun addEofToken() {
        tokens.add(Token(EOF, "", null, line, IntRange.EMPTY, IntRange.EMPTY))
    }


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