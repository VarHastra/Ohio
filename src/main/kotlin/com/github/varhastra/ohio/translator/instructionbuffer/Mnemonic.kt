package com.github.varhastra.ohio.translator.instructionbuffer

enum class Mnemonic(val symbol: String) {
    MOV("mov"),
    PUSH("push"),
    POP("pop"),
    ADD("add"),
    SUB("sub"),
    IMUL("imul"),
    IDIV("idiv"),
    NEG("neg")
}