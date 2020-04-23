package com.github.varhastra.ohio.interpreter

import com.github.varhastra.ohio.common.Position
import com.github.varhastra.ohio.common.exceptions.PositionAwareException
import com.github.varhastra.ohio.lexer.Token
import com.github.varhastra.ohio.lexer.TokenType.*
import com.github.varhastra.ohio.parser.Expr
import com.github.varhastra.ohio.parser.Expr.*
import com.github.varhastra.ohio.parser.Stmt
import com.github.varhastra.ohio.parser.Stmt.*

class Interpreter {

    class RuntimeFailureException(
        token: Token,
        message: String,
        cause: Throwable? = null
    ) : PositionAwareException(Position(token.line, token.columnRange), message, cause)


    var runtimeFailure: RuntimeFailureException? = null
        private set

    val hasErrors
        get() = runtimeFailure != null

    private var environment = Environment()


    fun interpret(program: List<Stmt>) {
        try {
            runtimeFailure = null
            run(program)
        } catch (e: RuntimeFailureException) {
            runtimeFailure = e
        }
    }

    private fun run(program: List<Stmt>) {
        program.forEach { execute(it) }
    }

    private fun execute(statement: Stmt) = when (statement) {
        is ExpressionStmt -> execute(statement)
        is BlockStmt -> execute(statement)
        is IfStmt -> execute(statement)
        is WhileStmt -> execute(statement)
        is RepeatStmt -> execute(statement)
        is PrintStmt -> execute(statement)
        is PrintlnStmt -> execute(statement)
    }

    private fun evaluate(expr: Expr) = when (expr) {
        is Literal -> evaluate(expr)
        is Var -> evaluate(expr)
        is Grouping -> evaluate(expr)
        is Unary -> evaluate(expr)
        is Binary -> evaluate(expr)
        is Logical -> evaluate(expr)
        is Assignment -> evaluate(expr)
    }

    private fun execute(stmt: ExpressionStmt) {
        evaluate(stmt.expression)
    }

    private fun execute(stmt: BlockStmt) {
        executeBlock(stmt, Environment(environment))
    }

    private fun executeBlock(stmt: BlockStmt, environment: Environment) {
        val outer = this.environment
        try {
            this.environment = environment
            stmt.statements.forEach { statement ->
                execute(statement)
            }
        } finally {
            this.environment = outer
        }
    }

    private fun execute(stmt: IfStmt) {
        val conditionValue = evaluate(stmt.condition)
        checkBoolean(stmt.token, conditionValue)
        conditionValue as Boolean
        if (conditionValue) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    private fun execute(stmt: WhileStmt) {
        var conditionValue = evaluate(stmt.condition)
        checkBoolean(stmt.token, conditionValue)
        while (conditionValue as Boolean) {
            execute(stmt.body)
            conditionValue = evaluate(stmt.condition)
        }
    }

    private fun execute(stmt: RepeatStmt) {
        var conditionValue = evaluate(stmt.condition)
        checkBoolean(stmt.token, conditionValue)
        do {
            execute(stmt.body)
            conditionValue = evaluate(stmt.condition)
        } while (conditionValue as Boolean)
    }

    private fun execute(stmt: PrintStmt) {
        val value = evaluate(stmt.expression)
        print(stringify(value))
    }

    private fun execute(stmt: PrintlnStmt) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    private fun evaluate(expr: Literal): Any {
        return expr.value
    }

    private fun evaluate(expr: Var): Any {
        return try {
            environment.get(expr.identifier.lexeme)
        } catch (e: Environment.UnresolvedIdentifierException) {
            throw RuntimeFailureException(expr.identifier, "Unresolved identifier encountered.", e)
        }
    }

    private fun evaluate(expr: Grouping): Any {
        return evaluate(expr.expr)
    }

    private fun evaluate(expr: Unary): Any {
        val (operator, expression) = expr
        val result = evaluate(expression)
        return when (operator.type) {
            NOT -> {
                checkBoolean(operator, result)
                !(result as Boolean)
            }
            MINUS -> {
                checkNumber(operator, result)
                -(result as Long)
            }
            else -> throw RuntimeFailureException(operator, "Unsupported operation.")
        }
    }

    private fun evaluate(expr: Binary): Any {
        return when (expr.operator.type) {
            PLUS -> add(expr)
            MINUS -> sub(expr)
            STAR -> mul(expr)
            SLASH -> div(expr)
            MOD -> mod(expr)
            GREATER -> greater(expr)
            LESS -> less(expr)
            GREATER_EQUAL -> greaterEqual(expr)
            LESS_EQUAL -> lessEqual(expr)
            EQUAL_EQUAL -> equal(expr)
            BANG_EQUAL -> notEqual(expr)
            else -> throw RuntimeFailureException(expr.operator, "Unsupported operator.")
        }
    }

    private fun greater(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        return left > right
    }

    private fun less(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        return left < right
    }

    private fun greaterEqual(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        return left >= right
    }

    private fun lessEqual(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        return left <= right
    }

    private fun equal(expr: Binary): Any {
        val (l, _, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        return left == right
    }

    private fun notEqual(expr: Binary): Any {
        return !(equal(expr) as Boolean)
    }

    private fun add(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        return if (left is String || right is String) {
            stringify(left) + stringify(right)
        } else {
            checkNumber(operator, left, right)
            left as Long
            right as Long
            left + right
        }
    }

    private fun sub(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long
        return left - right
    }

    private fun mul(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long
        return left * right
    }

    private fun div(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        if (right == 0L) {
            throw RuntimeFailureException(expr.operator, "Division by zero.")
        }

        return left / right
    }

    private fun mod(expr: Binary): Any {
        val (l, operator, r) = expr
        val left = evaluate(l)
        val right = evaluate(r)

        checkNumber(operator, left, right)
        left as Long
        right as Long

        if (right == 0L) {
            throw RuntimeFailureException(expr.operator, "Division by zero.")
        }

        return left % right
    }

    private fun evaluate(expr: Logical): Any {
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
            else -> throw RuntimeFailureException(operator, "Unsupported operation.")
        }
    }

    private fun evaluate(expr: Assignment): Any {
        val value = evaluate(expr.value)
        environment.assign(expr.identifier.lexeme, value)
        return value
    }

    private fun and(expr: Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(expr.operator, leftResult)
        leftResult as Boolean
        return if (!leftResult) {
            leftResult
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(expr.operator, rightResult)
            rightResult
        }
    }

    private fun nand(expr: Logical): Any {
        return !(and(expr) as Boolean)
    }

    private fun or(expr: Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(expr.operator, leftResult)
        leftResult as Boolean
        return if (leftResult) {
            leftResult
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(expr.operator, rightResult)
            rightResult
        }
    }

    private fun nor(expr: Logical): Any {
        return !(or(expr) as Boolean)
    }

    private fun xor(expr: Logical): Any {
        val leftResult = evaluate(expr.left)
        val rightResult = evaluate(expr.right)
        checkBoolean(expr.operator, leftResult, rightResult)
        leftResult as Boolean
        rightResult as Boolean
        return (leftResult && !rightResult) || (!leftResult && rightResult)
    }

    private fun xnor(expr: Logical): Any {
        return !(xor(expr) as Boolean)
    }

    private fun imp(expr: Logical): Any {
        val leftResult = evaluate(expr.left)
        checkBoolean(expr.operator, leftResult)
        leftResult as Boolean
        return if (!leftResult) {
            true
        } else {
            val rightResult = evaluate(expr.right)
            checkBoolean(expr.operator, rightResult)
            rightResult
        }
    }

    private fun nimp(expr: Logical): Any {
        return !(imp(expr) as Boolean)
    }

    private fun checkNumber(operator: Token, vararg values: Any) {
        values.forEach { value ->
            if (value !is Long) {
                throw RuntimeFailureException(operator, "Unexpected type. Integer was expected.")
            }
        }
    }

    private fun checkBoolean(operator: Token, vararg values: Any) {
        for (value in values) {
            if (value !is Boolean) {
                throw RuntimeFailureException(operator, "Unexpected type. Boolean was expected.")
            }
        }
    }

    private fun stringify(obj: Any): String {
        return when (obj) {
            is String -> obj
            is Long -> obj.toString()
            is Boolean -> {
                return if (obj) "true" else "false"
            }
            else -> obj.toString()
        }
    }
}