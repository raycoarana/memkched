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
) : Operation<TextProtocolSocketChannelWrapper, AddReplaceResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): AddReplaceResult {
        val cmd = "replace $key ${flags.toUShort()} ${expiration.value} ${data.size}${reply.asTextCommandValue()}"
        socketChannelWrapper.writeLine(cmd)
        socketChannelWrapper.writeBinary(data)

        if (reply == Reply.NO_REPLY) {
            return AddReplaceResult.NoReply
        }

        return when (val result = socketChannelWrapper.readLine()) {
            STORED -> AddReplaceResult.Stored
            NOT_STORED -> AddReplaceResult.NotStored
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
