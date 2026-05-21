package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.IncrDecrResult.NoReply
import com.raycoarana.memkched.internal.result.IncrDecrResult.NotFound
import com.raycoarana.memkched.internal.text.NOT_FOUND
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class DecrOperation(
    private val key: String,
    private val value: ULong,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, IncrDecrResult>(), TextOperation<IncrDecrResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLine(command())
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): IncrDecrResult {
        val result = socketChannelWrapper.readLine()
        return when {
            result == NOT_FOUND -> NotFound
            result[0].isDigit() -> IncrDecrResult.Value(result.toULong())
            else -> throw MemcachedError.parse(result).asException()
        }
    }

    override fun noReplyResult() = NoReply

    private fun command() = "decr $key $value${reply.asTextCommandValue()}"
}
