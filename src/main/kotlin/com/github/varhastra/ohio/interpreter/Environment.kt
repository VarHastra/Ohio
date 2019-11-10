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
        val env = findScopeOfDeclarationFor(name)
        if (env != null) {
            env.variables[name] = value
        } else {
            variables[name] = value
        }
    }

    private fun findScopeOfDeclarationFor(name: String): Environment? {
        var env: Environment? = this
        while (env != null) {
            if (env.isDeclaredInCurrentScope(name)) {
                return env
            }
            env = env.enclosing
        }
        return null
    }

    private fun isDeclaredInCurrentScope(name: String): Boolean {
        return name in variables
    }

    fun get(name: String): Any {
        return when {
            name in variables -> variables[name]!!
            enclosing != null -> enclosing.get(name)
            else -> throw UnresolvedIdentifierException("Unresolved identifier: '$name'.")
        }
    }
}