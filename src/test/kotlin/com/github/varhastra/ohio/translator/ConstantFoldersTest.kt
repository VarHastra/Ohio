package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.Lexer
import com.github.varhastra.ohio.parser.Expr.Assignment
import com.github.varhastra.ohio.parser.Expr.Literal
import com.github.varhastra.ohio.parser.Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConstantFoldersTest {

    @Test
    fun `leaves int literal intact`() {
        val expr = Literal(5)

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves string literal intact`() {
        val expr = Literal("literal")

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves boolean literal intact`() {
        val expr = Literal("true")

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves variable intact`() {
        val exprStr = "a"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `folds grouping`() {
        val exprStr = "(5)"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(5), actual)
    }

    @Test
    fun `folds int unary minus`() {
        val exprStr = "-5"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(-5), actual)
    }

    @Test
    fun `folds int addition`() {
        val exprStr = "5 + 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(7), actual)
    }

    @Test
    fun `folds int subtraction`() {
        val exprStr = "5 - 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(3), actual)
    }

    @Test
    fun `folds int multiplication`() {
        val exprStr = "5 * 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(10), actual)
    }

    @Test
    fun `folds int division`() {
        val exprStr = "5 / 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(2), actual)
    }

    @Test
    fun `folds int modulo`() {
        val exprStr = "5 % 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(1), actual)
    }

    @Test
    fun `folds int expression`() {
        val exprStr = "(5 * 2) + (9 * (40 / (6 % 4) - 10))"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(Literal(100), actual)
    }

    @Test
    fun `leaves logical NOT intact`() {
        val exprStr = "not false"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves logical AND intact`() {
        val exprStr = "true and false"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves logical OR intact`() {
        val exprStr = "true or false"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves logical XOR intact`() {
        val exprStr = "true xor false"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `leaves string concatenation intact`() {
        val exprStr = "\"literal\" + \"another literal\""
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertEquals(expr, actual)
    }

    @Test
    fun `folds right side of assignment`() {
        val exprStr = "a := 5 + 2"
        val expr = Parser(Lexer(exprStr).lex()).parseExpression()

        val actual = expr.fold()

        assertTrue(actual is Assignment)
        assertEquals(Literal(7), (actual as Assignment).value)
    }
}