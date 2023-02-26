package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.DeleteResult.Deleted
import com.raycoarana.memkched.internal.result.DeleteResult.NoReply
import com.raycoarana.memkched.internal.result.DeleteResult.NotFound
import com.raycoarana.memkched.internal.text.DELETED
import com.raycoarana.memkched.internal.text.NOT_FOUND
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class DeleteOperation(
    private val key: String,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, DeleteResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): DeleteResult {
        val cmd = "delete $key${reply.asTextCommandValue()}"
        socketChannelWrapper.writeLine(cmd)

        if (reply == Reply.NO_REPLY) {
            return NoReply
        }

        return when (val result = socketChannelWrapper.readLine()) {
            DELETED -> Deleted
            NOT_FOUND -> NotFound
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
