package com.github.varhastra.ohio.parser

import com.github.varhastra.ohio.lexer.Token

sealed class Stmt {

    data class ExpressionStatement(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    data class BlockStmt(val statements: List<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class IfStmt(val token: Token, val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class WhileStmt(val token: Token, val condition: Expr, val body: BlockStmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class RepeatStmt(val token: Token, val condition: Expr, val body: BlockStmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class PrintStmt(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    data class PrintlnStmt(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    abstract fun <R> accept(visitor: Visitor<R>): R


    interface Visitor<R> {
        fun visit(stmt: ExpressionStatement): R
        fun visit(stmt: BlockStmt): R
        fun visit(stmt: IfStmt): R
        fun visit(stmt: WhileStmt): R
        fun visit(stmt: RepeatStmt): R
        fun visit(stmt: PrintStmt): R
        fun visit(stmt: PrintlnStmt): R
    }
}