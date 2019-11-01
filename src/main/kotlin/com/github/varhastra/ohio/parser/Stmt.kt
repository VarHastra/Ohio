package com.github.varhastra.ohio.parser

sealed class Stmt {

    data class ExpressionStatement(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
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
        fun visit(stmt: PrintStmt): R
        fun visit(stmt: PrintlnStmt): R
    }
}