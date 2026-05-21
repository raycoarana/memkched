package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.text.NOT_STORED
import com.raycoarana.memkched.internal.text.STORED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class AppendOperation(
    private val key: String,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, AppendPrependResult>(), TextOperation<AppendPrependResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLineAndBinary(command(), data)
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): AppendPrependResult =
        when (val result = socketChannelWrapper.readLine()) {
            STORED -> AppendPrependResult.Stored
            NOT_STORED -> AppendPrependResult.NotStored
            else -> throw MemcachedError.parse(result).asException()
        }

    override fun noReplyResult() = AppendPrependResult.NoReply

    private fun command() = "append $key 0 0 ${data.size}${reply.asTextCommandValue()}"
}
