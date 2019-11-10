package com.github.varhastra.ohio.parser

class AstPrinter(private val expression: Expr) : Expr.Visitor<String> {

    fun stringify(): String {
        return expression.accept(this)
    }

    fun print() {
        println(stringify())
    }

    override fun visit(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visit(expr: Expr.Var): String {
        return expr.identifier.lexeme
    }

    override fun visit(expr: Expr.Grouping): String {
        return "(${expr.expr.accept(this)})"
    }

    override fun visit(expr: Expr.Unary): String {
        return "(${expr.operator.lexeme} ${expr.right.accept(this)})"
    }

    override fun visit(expr: Expr.Logical): String {
        val (left, op, right) = expr
        return "(${left.accept(this)} ${op.lexeme} ${right.accept(this)})"
    }

    override fun visit(expr: Expr.Assignment): String {
        val (identifier, value) = expr
        return "(${identifier.lexeme} := ${value.accept(this)})"
    }

    override fun visit(expr: Expr.Binary): String {
        TODO("Not implemented")
    }
}