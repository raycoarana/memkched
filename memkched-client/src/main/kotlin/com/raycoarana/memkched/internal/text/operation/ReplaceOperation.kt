package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.text.NOT_STORED
import com.raycoarana.memkched.internal.text.STORED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class ReplaceOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, AddReplaceResult>(), TextOperation<AddReplaceResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLineAndBinary(command(), data)
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): AddReplaceResult =
        when (val result = socketChannelWrapper.readLine()) {
            STORED -> AddReplaceResult.Stored
            NOT_STORED -> AddReplaceResult.NotStored
            else -> throw MemcachedError.parse(result).asException()
        }

    override fun noReplyResult() = AddReplaceResult.NoReply

    private fun command() =
        "replace $key ${flags.toUShort()} ${expiration.value} ${data.size}${reply.asTextCommandValue()}"
}
