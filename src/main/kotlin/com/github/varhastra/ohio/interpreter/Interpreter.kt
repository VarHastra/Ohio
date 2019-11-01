package com.github.varhastra.ohio.interpreter

import com.github.varhastra.ohio.lexer.TokenType.*
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Stmt

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {

    private val environment = Environment()

    fun interpret(program: List<Stmt>) {
        run(program)
    }

    private fun run(program: List<Stmt>) {
        program.forEach { execute(it) }
    }

    private fun execute(statement: Stmt) {
        return statement.accept(this)
    }

    private fun evaluate(expr: Expr): Any {
        return expr.accept(this)
    }

    override fun visit(stmt: Stmt.ExpressionStatement) {
        evaluate(stmt.expression)
    }

    override fun visit(stmt: Stmt.PrintStmt) {
        print(evaluate(stmt.expression))
    }

    override fun visit(stmt: Stmt.PrintlnStmt) {
        println(evaluate(stmt.expression))
    }

    override fun visit(expr: Expr.Literal): Any {
        return expr.value
    }

    override fun visit(expr: Expr.Var): Any {
        return environment.get(expr.identifier.lexeme)
    }

    override fun visit(expr: Expr.Grouping): Any {
        return evaluate(expr.expr)
    }

    override fun visit(expr: Expr.Unary): Any {
        val (operator, expression) = expr
        val result = evaluate(expression)
        return when (operator.type) {
            NOT -> {
                checkBoolean(result)
                !(result as Boolean)
            }
            else -> throw RuntimeException("Unsupported operation") // TODO: throw properly
        }
    }

    override fun visit(expr: Expr.Logical): Any {
        val operator = expr.operator
        return when (operator.type) {
            AND -> and(expr)
            NAND -> nand(expr)
            XOR -> xor(expr)
            XNOR -> xnor(expr)
            OR -> or(expr)
            NOR -> nor(expr)
            IMP -> imp(expr)
            NIMP -> nimp(expr)
            else -> throw RuntimeException("Unsupported operation") // TODO: throw properly
        }
    }

    override fun visit(expr: Expr.Assignment): Any {
        val value = evaluate(expr.value)
        environment.assign(expr.identifier.lexeme, value)
        return value
    }

    private fun and(expr: Expr.Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(leftResult)
        leftResult as Boolean
        return if (!leftResult) {
            leftResult
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(rightResult)
            rightResult
        }
    }

    private fun nand(expr: Expr.Logical): Any {
        return !(and(expr) as Boolean)
    }

    private fun or(expr: Expr.Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(leftResult)
        leftResult as Boolean
        return if (leftResult) {
            leftResult
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(rightResult)
            rightResult
        }
    }

    private fun nor(expr: Expr.Logical): Any {
        return !(or(expr) as Boolean)
    }

    private fun xor(expr: Expr.Logical): Any {
        val leftResult = evaluate(expr.left)
        val rightResult = evaluate(expr.right)
        checkBoolean(leftResult, rightResult)
        leftResult as Boolean
        rightResult as Boolean
        return (leftResult && !rightResult) || (!leftResult && rightResult)
    }

    private fun xnor(expr: Expr.Logical): Any {
        return !(xor(expr) as Boolean)
    }

    private fun imp(expr: Expr.Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(leftResult)
        leftResult as Boolean
        return if (!leftResult) {
            true
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(rightResult)
            rightResult
        }
    }

    private fun nimp(expr: Expr.Logical): Any {
        return !(imp(expr) as Boolean)
    }

    private fun checkBoolean(vararg values: Any) {
        for (value in values) {
            if (value !is Boolean) {
                throw RuntimeException("Unexpected type") // TODO: throw properly
            }
        }
    }
}