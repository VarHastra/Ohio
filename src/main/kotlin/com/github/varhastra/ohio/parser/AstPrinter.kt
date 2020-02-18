package com.github.varhastra.ohio.parser

class AstPrinter(private val expression: Expr) {

    fun stringify() = stringify(expression)

    fun print() {
        print(stringify())
    }

    fun println() {
        println(stringify())
    }

    private fun stringify(expression: Expr) = when (expression) {
        is Expr.Literal -> visit(expression)
        is Expr.Var -> visit(expression)
        is Expr.Grouping -> visit(expression)
        is Expr.Unary -> visit(expression)
        is Expr.Binary -> visit(expression)
        is Expr.Logical -> visit(expression)
        is Expr.Assignment -> visit(expression)
    }

    private fun visit(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    private fun visit(expr: Expr.Var): String {
        return expr.identifier.lexeme
    }

    private fun visit(expr: Expr.Grouping): String {
        return "(${stringify(expr.expr)})"
    }

    private fun visit(expr: Expr.Unary): String {
        return "(${expr.operator.lexeme} ${stringify(expr.right)})"
    }

    private fun visit(expr: Expr.Logical): String {
        val (left, op, right) = expr
        return "(${stringify(left)} ${op.lexeme} ${stringify(right)})"
    }

    private fun visit(expr: Expr.Assignment): String {
        val (identifier, value) = expr
        return "(${identifier.lexeme} := ${stringify(value)})"
    }

    private fun visit(expr: Expr.Binary): String {
        val (left, op, right) = expr
        return "(${stringify(left)} ${op.lexeme} ${stringify(right)})"
    }
}