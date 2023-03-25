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
) : Operation<TextProtocolSocketChannelWrapper, SetResult>() {
    override suspend fun run(socket: TextProtocolSocketChannelWrapper): SetResult {
        val cmd = "set $key ${flags.toUShort()} ${expiration.value} ${data.size}${reply.asTextCommandValue()}"
        socket.writeLine(cmd)
        socket.writeBinary(data)

        if (reply == Reply.NO_REPLY) {
            return SetResult.NoReply
        }

        val result = socket.readLine()
        return if (result == STORED) {
            SetResult.Stored
        } else {
            throw MemcachedError.parse(result).asException()
        }
    }
}
