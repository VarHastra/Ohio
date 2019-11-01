package com.github.varhastra.ohio.parser

import com.github.varhastra.ohio.lexer.Token

sealed class Expr {
    data class Literal(val value: Any) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Var(val identifier: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Grouping(val expr: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Assignment(val identifier: Token, val value: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    abstract fun <R> accept(visitor: Visitor<R>): R


    interface Visitor<R> {
        fun visit(expr: Literal): R
        fun visit(expr: Var): R
        fun visit(expr: Grouping): R
        fun visit(expr: Unary): R
        fun visit(expr: Logical): R
        fun visit(expr: Assignment): R
    }
}