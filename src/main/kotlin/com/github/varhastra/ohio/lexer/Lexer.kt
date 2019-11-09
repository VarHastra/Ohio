package com.github.varhastra.ohio.lexer

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.lexer.TokenType.*
import com.github.varhastra.ohio.lexer.exceptions.LexException
import com.github.varhastra.ohio.lexer.exceptions.UnexpectedSymbolException
import com.github.varhastra.ohio.lexer.exceptions.ValueOutOfRangeException

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
                val sourceRange = matcher.start() until matcher.end()
                when (type) {
                    NUMBER -> addNumberToken(sourceRange)
                    STRING -> addStringToken(sourceRange)
                    IDENTIFIER -> if (lexeme in keywords) {
                        addToken(keywords.getValue(lexeme), sourceRange)
                    }
                    else -> addToken(type, sourceRange)
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

    private fun addNumberToken(range: IntRange) {
        val literal = source.substring(range).filter { it != '_' }
        try {
            addToken(NUMBER, range, literal.toLong())
        } catch (e: NumberFormatException) {
            val columnRange = sourceRangeToColumnRange(range)
            val position = Position(currentLine, columnRange)
            _lexExceptions.add(
                ValueOutOfRangeException(
                    position,
                    "The value is out of range. Must be in [-2^63+1..2^63-1]."
                )
            )
        }
    }

    private fun addStringToken(range: IntRange) {
        val s = range.first + 1
        val e = range.last - 1
        val literal = source.substring(s..e)
        addToken(STRING, range, literal)
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
            "[{]".toPattern() to LEFT_BRACE,
            "[}]".toPattern() to RIGHT_BRACE,
            "[+]".toPattern() to PLUS,
            "-".toPattern() to MINUS,
            "[*]".toPattern() to STAR,
            "/".toPattern() to SLASH,
            "%".toPattern() to MOD,
            ";".toPattern() to SEMICOLON,
            "==".toPattern() to EQUAL_EQUAL,
            "!=".toPattern() to BANG_EQUAL,
            ":=".toPattern() to COLON_EQUAL,
            ">=".toPattern() to GREATER_EQUAL,
            "<=".toPattern() to LESS_EQUAL,
            ">".toPattern() to GREATER,
            "<".toPattern() to LESS,
            "[_a-zA-Z][_a-zA-Z0-9]*".toPattern() to IDENTIFIER,
            "[0-9][_0-9]*".toPattern() to NUMBER,
            "\"[^\n]*\"".toPattern() to STRING
        )

        private val keywords = mapOf(
            "true" to TRUE,
            "false" to FALSE,
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
            "not" to NOT,
            "if" to IF,
            "else" to ELSE,
            "while" to WHILE,
            "repeat" to REPEAT
        )

        private val newlinePattern = "\n".toPattern()

        private val spacePattern = "[ \t\r]+".toPattern()
    }
}