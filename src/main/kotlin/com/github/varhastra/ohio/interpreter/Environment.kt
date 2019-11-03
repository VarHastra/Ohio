package com.github.varhastra.ohio.interpreter

class Environment {

    class UnresolvedIdentifierException(
        message: String,
        cause: Throwable? = null,
        enableSuppression: Boolean = false,
        writableStackTrace: Boolean = true
    ) : RuntimeException(message, cause, enableSuppression, writableStackTrace)


    private val variables = mutableMapOf<String, Any>()

    fun assign(name: String, value: Any) {
        variables[name] = value
    }

    fun get(name: String): Any {
        return variables.getOrElse(name) {
            throw UnresolvedIdentifierException("Unresolved identifier: '$name'.")
        }
    }
}