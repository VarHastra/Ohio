package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*


private val foldableBinaryOperators = setOf(
    TokenType.PLUS,
    TokenType.MINUS,
    TokenType.STAR,
    TokenType.SLASH,
    TokenType.MOD
)

private val foldableUnaryOperators = setOf(
    TokenType.MINUS
)

fun Expr.fold() = foldInternal(this)

private fun foldInternal(expr: Expr): Expr {
    return when (expr) {
        is Grouping -> foldGrouping(expr)
        is Unary -> foldUnary(expr)
        is Binary -> foldBinary(expr)
        is Logical -> foldLogical(expr)
        is Assignment -> foldAssignment(expr)
        else -> expr
    }
}

private fun foldGrouping(expr: Grouping): Expr {
    val nestedExpr = foldInternal(expr.expr)
    return when (nestedExpr) {
        is Literal -> nestedExpr
        else -> expr.copy(expr = nestedExpr)
    }
}

private fun foldUnary(expr: Unary): Expr {
    val right = foldInternal(expr.right)
    val operator = expr.operator.type
    return when {
        right is Literal && canBeFoldedAsUnary(operator, right) -> Literal(evaluate(operator, right))
        else -> expr.copy(right = right)
    }
}

private fun foldBinary(expr: Binary): Expr {
    val left = foldInternal(expr.left)
    val right = foldInternal(expr.right)
    val operator = expr.operator.type
    return when {
        left is Literal && right is Literal && canBeFoldedAsBinary(left, operator, right) -> Literal(
            evaluate(
                left,
                operator,
                right
            )
        )
        else -> expr.copy(left = left, right = right)
    }
}

private fun foldLogical(expr: Logical): Expr {
    val left = foldInternal(expr.left)
    val right = foldInternal(expr.right)
    return expr.copy(left = left, right = right)
}

private fun foldAssignment(expr: Assignment): Expr {
    val value = foldInternal(expr.value)
    return expr.copy(value = value)
}

private fun evaluate(left: Literal, operator: TokenType, right: Literal): Int {
    checkAllAreNumbers(left.value, right.value)
    val leftInt = left.value as Int
    val rightInt = right.value as Int

    return when (operator) {
        TokenType.PLUS -> leftInt + rightInt
        TokenType.MINUS -> leftInt - rightInt
        TokenType.STAR -> leftInt * rightInt
        TokenType.SLASH -> {
            if (rightInt == 0) {
                throw IllegalArgumentException("Division by zero can't be folded.")
            }
            leftInt / rightInt
        }
        TokenType.MOD -> {
            if (rightInt == 0) {
                throw IllegalArgumentException("Division by zero can't be folded.")
            }
            leftInt % rightInt
        }
        else -> throw IllegalArgumentException("Binary operator $operator can't be folded.")
    }
}

private fun evaluate(operator: TokenType, right: Literal): Int {
    checkAllAreNumbers(right.value)
    val rightInt = right.value as Int

    return when (operator) {
        TokenType.MINUS -> -rightInt
        else -> throw IllegalArgumentException("Unary operator $operator can't be folded.")
    }
}

private fun checkAllAreNumbers(vararg values: Any) {
    values.forEach { value ->
        if (value !is Int) {
            throw IllegalArgumentException("Only integers are supported. $value is not an integer.")
        }
    }
}

private fun canBeFoldedAsBinary(left: Literal, operator: TokenType, right: Literal): Boolean {
    return left.value is Int &&
            right.value is Int &&
            operator in foldableBinaryOperators &&
            !isDivisionByZero(operator, right)
}

private fun canBeFoldedAsUnary(operator: TokenType, right: Literal): Boolean {
    return operator in foldableUnaryOperators && right.value is Int
}

private fun isDivisionByZero(operator: TokenType, right: Literal): Boolean {
    return (operator == TokenType.SLASH || operator == TokenType.MOD) && right.value == 0
}