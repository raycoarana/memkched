package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal interface TextOperation<out R> {
    val readsResponse: Boolean
        get() = true

    suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper)
    suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): R
    fun noReplyResult(): R = error("Operation expects a response")
}
