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
) : Operation<TextProtocolSocketChannelWrapper, DeleteResult>(), TextOperation<DeleteResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLine(command())
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): DeleteResult =
        when (val result = socketChannelWrapper.readLine()) {
            DELETED -> Deleted
            NOT_FOUND -> NotFound
            else -> throw MemcachedError.parse(result).asException()
        }

    override fun noReplyResult() = NoReply

    private fun command() = "delete $key${reply.asTextCommandValue()}"
}
