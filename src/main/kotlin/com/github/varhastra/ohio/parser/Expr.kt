package com.github.varhastra.ohio.parser

import com.github.varhastra.ohio.lexer.Token

sealed class Expr {
    data class Literal(val value: Any) : Expr()

    data class Var(val identifier: Token) : Expr()

    data class Grouping(val expr: Expr) : Expr()

    data class Unary(val operator: Token, val right: Expr) : Expr()

    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()

    data class Assignment(val identifier: Token, val value: Expr) : Expr()
}