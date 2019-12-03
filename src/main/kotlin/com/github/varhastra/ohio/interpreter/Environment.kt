package com.github.varhastra.ohio.interpreter

class Environment(private val enclosing: Environment? = null) {

    class UnresolvedIdentifierException(
        message: String,
        cause: Throwable? = null,
        enableSuppression: Boolean = false,
        writableStackTrace: Boolean = true
    ) : RuntimeException(message, cause, enableSuppression, writableStackTrace)


    private val variables = mutableMapOf<String, Any>()

    fun assign(name: String, value: Any) {
        var env: Environment? = this
        var declarationFound = false
        while (env != null && !declarationFound) {
            declarationFound = env.variables.replace(name, value) != null
            env = env.enclosing
        }

        if (!declarationFound) {
            this.variables[name] = value
        }
    }

    fun get(name: String): Any {
        return variables[name] ?: when {
            enclosing != null -> enclosing.get(name)
            else -> throw UnresolvedIdentifierException("Unresolved identifier: '$name'.")
        }
    }
}