package com.github.varhastra.ohio.interpreter

class Environment {
    private val variables = mutableMapOf<String, Any>()

    fun assign(name: String, value: Any) {
        variables[name] = value
    }

    fun get(name: String): Any {
        return variables.getOrElse(name) {
            throw RuntimeException("Unknown identifier: '$name'.")
        }
    }
}