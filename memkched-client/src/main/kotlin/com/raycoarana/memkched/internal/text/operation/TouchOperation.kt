package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.TouchResult
import com.raycoarana.memkched.internal.result.TouchResult.NoReply
import com.raycoarana.memkched.internal.result.TouchResult.NotFound
import com.raycoarana.memkched.internal.result.TouchResult.Touched
import com.raycoarana.memkched.internal.text.NOT_FOUND
import com.raycoarana.memkched.internal.text.TOUCHED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class TouchOperation(
    private val key: String,
    private val expiration: Expiration,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, TouchResult>() {
    override suspend fun run(socket: TextProtocolSocketChannelWrapper): TouchResult {
        val cmd = "touch $key ${expiration.value}${reply.asTextCommandValue()}"
        socket.writeLine(cmd)

        if (reply == Reply.NO_REPLY) {
            return NoReply
        }

        return when (val result = socket.readLine()) {
            TOUCHED -> Touched
            NOT_FOUND -> NotFound
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
