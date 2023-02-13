package com.raycoarana.memkched.api

@JvmInline
value class Reply internal constructor(internal val value: String) {
    fun asTextCommandValue() = if (value.isEmpty()) value else " $value"
    companion object {
        val DEFAULT = Reply("")
        val NO_REPLY = Reply("noreply")
    }
}
