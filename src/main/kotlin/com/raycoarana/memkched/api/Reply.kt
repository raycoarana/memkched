package com.raycoarana.memkched.api

@JvmInline
value class Reply internal constructor(val value: String) {
    companion object {
        val DEFAULT = Reply("")
        val NO_REPLY = Reply(" noreply")
    }
}
