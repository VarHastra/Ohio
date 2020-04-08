package com.github.varhastra.ohio.translator

import com.github.varhastra.ohio.parser.Expr

class IdentifiersCollector {

    private val identifiers = mutableSetOf<String>()

    fun collectIdentifiers(expr: Expr): Set<String> {
        identifiers.clear()
        process(expr)
        return identifiers.toSet()
    }

    private fun process(expr: Expr) {
        when (expr) {
            is Expr.Literal -> {
            }
            is Expr.Grouping -> process(expr)
            is Expr.Unary -> process(expr)
            is Expr.Binary -> process(expr)
            is Expr.Logical -> process(expr)
            is Expr.Var -> process(expr)
            is Expr.Assignment -> process(expr)
        }
    }

    private fun process(expr: Expr.Grouping) {
        process(expr.expr)
    }

    private fun process(expr: Expr.Unary) {
        process(expr.right)
    }

    private fun process(expr: Expr.Binary) {
        process(expr.left)
        process(expr.right)
    }

    private fun process(expr: Expr.Var) {
        identifiers.add(expr.identifier.lexeme)
    }

    private fun process(expr: Expr.Logical) {
        process(expr.left)
        process(expr.right)
    }

    private fun process(expr: Expr.Assignment) {
        process(expr.value)
        identifiers.add(expr.identifier.lexeme)
    }
}