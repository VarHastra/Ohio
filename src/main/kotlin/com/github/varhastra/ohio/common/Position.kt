package com.github.varhastra.ohio.common

data class Position(val line: Int, val columnRange: IntRange) {
    val firstColumn
        get() = columnRange.first

    val lastColumn
        get() = columnRange.last
}