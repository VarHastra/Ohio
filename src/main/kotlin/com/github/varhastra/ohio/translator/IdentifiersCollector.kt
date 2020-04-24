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
            is Expr.Grouping -> processGrouping(expr)
            is Expr.Unary -> processUnary(expr)
            is Expr.Binary -> processBinary(expr)
            is Expr.Logical -> processLogical(expr)
            is Expr.Var -> processVar(expr)
            is Expr.Assignment -> processAssignment(expr)
        }
    }

    private fun processGrouping(expr: Expr.Grouping) {
        process(expr.expr)
    }

    private fun processUnary(expr: Expr.Unary) {
        process(expr.right)
    }

    private fun processBinary(expr: Expr.Binary) {
        process(expr.left)
        process(expr.right)
    }

    private fun processVar(expr: Expr.Var) {
        identifiers.add(expr.identifier.lexeme)
    }

    private fun processLogical(expr: Expr.Logical) {
        process(expr.left)
        process(expr.right)
    }

    private fun processAssignment(expr: Expr.Assignment) {
        process(expr.value)
        identifiers.add(expr.identifier.lexeme)
    }
}