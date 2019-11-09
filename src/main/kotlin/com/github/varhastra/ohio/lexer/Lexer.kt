package com.github.varhastra.ohio.lexer

import com.github.varhastra.ohio.lexer.TokenType.*
import com.github.varhastra.ohio.lexer.exceptions.LexException
import com.github.varhastra.ohio.lexer.exceptions.UnexpectedSymbolException

class Lexer(private val source: String) {

    val lexExceptions: List<LexException>
        get() = _lexExceptions
    private val _lexExceptions = mutableListOf<LexException>()

    val hasErrors
        get() = _lexExceptions.size > 0

    private val tokens = mutableListOf<Token>()

    private var currentPos = 0
    private var currentLine = 0
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

        registerError()
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
            currentLine++
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

    private fun registerError() {
        val columnRange = sourceRangeToColumnRange(currentPos..currentPos)
        val position = Position(currentLine, columnRange)
        _lexExceptions.add(UnexpectedSymbolException(position, "Unexpected symbol encountered at $position."))
        currentPos++
    }

    private fun addToken(type: TokenType, range: IntRange, literal: Any? = null) {
        val lexeme = source.substring(range)
        val columnRange = (range.first - numOfCharsBeforeCurrentLine)..(range.last - numOfCharsBeforeCurrentLine)
        tokens.add(Token(type, lexeme, literal, currentLine, columnRange, range))
    }

    private fun addEofToken() {
        tokens.add(Token(EOF, "", null, currentLine, IntRange.EMPTY, IntRange.EMPTY))
    }

    private fun sourceRangeToColumnRange(range: IntRange): IntRange {
        val start = range.first - numOfCharsBeforeCurrentLine
        val end = range.last - numOfCharsBeforeCurrentLine
        return start..end
    }

    private fun columnRangeToSourceRange(range: IntRange): IntRange {
        val start = range.first + numOfCharsBeforeCurrentLine
        val end = range.last + numOfCharsBeforeCurrentLine
        return start..end
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