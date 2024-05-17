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
) : Operation<TextProtocolSocketChannelWrapper, AppendPrependResult>() {
    override suspend fun run(socket: TextProtocolSocketChannelWrapper): AppendPrependResult {
        val cmd = "append $key 0 0 ${data.size}${reply.asTextCommandValue()}"
        socket.writeLine(cmd)
        socket.writeBinary(data)

        if (reply == Reply.NO_REPLY) {
            return AppendPrependResult.NoReply
        }

        return when (val result = socket.readLine()) {
            STORED -> AppendPrependResult.Stored
            NOT_STORED -> AppendPrependResult.NotStored
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
