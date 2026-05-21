package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.text.STORED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class SetOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, SetResult>(), TextOperation<SetResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLineAndBinary(command(), data)
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): SetResult {
        val result = socketChannelWrapper.readLine()
        return if (result == STORED) SetResult.Stored else throw MemcachedError.parse(result).asException()
    }

    override fun noReplyResult() = SetResult.NoReply

    private fun command(): String {
        val cmd = "set $key ${flags.toUShort()} ${expiration.value} ${data.size}${reply.asTextCommandValue()}"
        return cmd
    }
}
