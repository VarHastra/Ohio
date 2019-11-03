package com.github.varhastra.ohio

enum class ExitCode(val value: Int) {
    USAGE_ERROR(64),
    DATA_FORMAT_ERROR(65),
    CAN_NOT_OPEN_INPUT(66)
}