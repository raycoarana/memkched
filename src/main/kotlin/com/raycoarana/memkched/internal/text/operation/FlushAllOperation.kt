package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.FlushAllResult
import com.raycoarana.memkched.internal.result.FlushAllResult.NoReply
import com.raycoarana.memkched.internal.result.FlushAllResult.Ok
import com.raycoarana.memkched.internal.text.OK
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class FlushAllOperation(
    private val expiration: Expiration?,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, FlushAllResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): FlushAllResult {
        val expirationValue = expiration?.value?.let { " $it" } ?: ""
        val cmd = "flush_all$expirationValue${reply.asTextCommandValue()}"
        socketChannelWrapper.writeLine(cmd)

        if (reply == Reply.NO_REPLY) {
            return NoReply
        }

        val result = socketChannelWrapper.readLine()
        return if (result == OK) {
            Ok
        } else {
            throw MemcachedError.parse(result).asException()
        }
    }
}
