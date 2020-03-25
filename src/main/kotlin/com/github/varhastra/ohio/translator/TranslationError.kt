package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.lexer.TokenType
import com.github.varhastra.ohio.parser.Expr

sealed class TranslationError(val msg: String) {
    data class UnsupportedExpression(val expr: Expr) : TranslationError("Unsupported expression: $expr.")
    data class UnsupportedOperation(val tokenType: TokenType) : TranslationError("Unsupported operation: $tokenType.")
    data class UnsupportedOperand(val operand: Any) : TranslationError("Unsupported operand: $operand.")
}