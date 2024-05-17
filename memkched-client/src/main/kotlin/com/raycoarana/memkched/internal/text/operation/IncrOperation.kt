package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.IncrDecrResult.NoReply
import com.raycoarana.memkched.internal.result.IncrDecrResult.NotFound
import com.raycoarana.memkched.internal.text.NOT_FOUND
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class IncrOperation(
    private val key: String,
    private val value: ULong,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, IncrDecrResult>() {
    override suspend fun run(socket: TextProtocolSocketChannelWrapper): IncrDecrResult {
        val cmd = "incr $key $value${reply.asTextCommandValue()}"
        socket.writeLine(cmd)

        if (reply == Reply.NO_REPLY) {
            return NoReply
        }

        val result = socket.readLine()
        return when {
            result == NOT_FOUND -> NotFound
            result[0].isDigit() -> IncrDecrResult.Value(result.toULong())
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
