package com.github.varhastra.ohio.parser

import com.github.varhastra.ohio.lexer.Token
import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.lexer.TokenType.*

class Parser(private val tokens: List<Token>) {

    class ParseException(
        val token: Token,
        message: String,
        cause: Throwable? = null,
        enableSuppression: Boolean = false,
        writableStackTrace: Boolean = true
    ) : RuntimeException(message, cause, enableSuppression, writableStackTrace)

    private val _parseExceptions = mutableListOf<ParseException>()
    val parseExceptions: List<ParseException>
        get() = _parseExceptions

    val hasErrors
        get() = _parseExceptions.isNotEmpty()

    private var currentPos = 0

    private val current
        get() = tokens[currentPos]

    private val previous
        get() = tokens[currentPos - 1]

    private val isAtEnd
        get() = current.type == EOF

    private val isNotAtEnd
        get() = !isAtEnd


    fun parseExpression(): Expr {
        return expression()
    }

    fun parse(): List<Stmt> {
        return program()
    }

    private fun program(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (isNotAtEnd) {
            try {
                statements.add(statement())
            } catch (e: ParseException) {
                _parseExceptions.add(e)
                synchronize()
            }
        }
        return statements
    }

    private fun statement(): Stmt {
        if (matchCurrentAgainst(PRINT)) return finishPrintStatement()
        if (matchCurrentAgainst(PRINTLN)) return finishPrintlnStatement()

        return expressionStatement()
    }

    private fun finishPrintStatement(): Stmt {
        val expression = expression()
        require(SEMICOLON, "';' was expected.")
        return Stmt.PrintStmt(expression)
    }

    private fun finishPrintlnStatement(): Stmt {
        val expression = expression()
        require(SEMICOLON, "';' was expected.")
        return Stmt.PrintlnStmt(expression)
    }

    private fun expressionStatement(): Stmt {
        val expression = expression()
        require(SEMICOLON, "';' was expected.")
        return Stmt.ExpressionStatement(expression)
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = logicalImp()

        return if (matchCurrentAgainst(COLON_EQUAL)) {
            val equalsSign = previous
            if (expr is Expr.Var) {
                val value = assignment()
                Expr.Assignment(expr.identifier, value)
            } else {
                throw ParseException(equalsSign, "Invalid assignment target.")
            }
        } else {
            expr
        }
    }

    private fun logicalImp(): Expr {
        val expr = logicalOr()

        return if (matchCurrentAgainst(IMP, NIMP)) {
            val operator = previous
            val right = logicalImp()
            Expr.Logical(expr, operator, right)
        } else {
            expr
        }
    }

    private fun logicalOr(): Expr {
        var expr = logicalXor()

        while (matchCurrentAgainst(OR, NOR)) {
            val operator = previous
            val right = logicalXor()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun logicalXor(): Expr {
        var expr = logicalAnd()

        while (matchCurrentAgainst(XOR, XNOR)) {
            val operator = previous
            val right = logicalAnd()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun logicalAnd(): Expr {
        var expr = unary()

        while (matchCurrentAgainst(AND, NAND)) {
            val operator = previous
            val right = unary()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        return if (matchCurrentAgainst(NOT)) {
            val operator = previous
            Expr.Unary(operator, unary())
        } else {
            primary()
        }
    }

    private fun primary(): Expr {
        if (matchCurrentAgainst(TRUE)) return Expr.Literal(true)
        if (matchCurrentAgainst(FALSE)) return Expr.Literal(false)
        if (matchCurrentAgainst(IDENTIFIER)) return Expr.Var(previous)

        if (matchCurrentAgainst(LEFT_PAREN)) {
            val expression = expression()
            require(RIGHT_PAREN, "')' was expected.")
            return Expr.Grouping(expression)
        }

        throw ParseException(current, "Unexpected expression.")
    }

    private fun matchCurrentAgainst(vararg targetTypes: TokenType): Boolean {
        for (targetType in targetTypes) {
            if (matchCurrentAgainst(targetType)) {
                return true
            }
        }

        return false
    }

    private fun matchCurrentAgainst(targetType: TokenType): Boolean {
        return if (checkCurrentAgainst(targetType)) {
            advance()
            true
        } else {
            false
        }
    }

    private fun require(targetType: TokenType, msg: String) {
        if (checkCurrentAgainst(targetType)) {
            advance()
            return
        } else {
            throw ParseException(current, msg)
        }
    }

    private fun checkCurrentAgainst(targetType: TokenType): Boolean {
        return isNotAtEnd && current.type == targetType
    }

    private fun advance() {
        if (isNotAtEnd) {
            currentPos++
        }
    }

    private fun synchronize() {
        while (!isAtEnd) {
            if (previous.type == SEMICOLON) {
                return
            }
            advance()
        }
    }
}