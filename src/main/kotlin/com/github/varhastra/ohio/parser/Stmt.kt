package com.github.varhastra.ohio.parser

import com.github.varhastra.ohio.lexer.Token

sealed class Stmt {

    data class ExpressionStmt(val expression: Expr) : Stmt()

    data class BlockStmt(val statements: List<Stmt>) : Stmt()

    data class IfStmt(val token: Token, val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()

    data class WhileStmt(val token: Token, val condition: Expr, val body: BlockStmt) : Stmt()

    data class RepeatStmt(val token: Token, val condition: Expr, val body: BlockStmt) : Stmt()

    data class PrintStmt(val expression: Expr) : Stmt()

    data class PrintlnStmt(val expression: Expr) : Stmt()
}