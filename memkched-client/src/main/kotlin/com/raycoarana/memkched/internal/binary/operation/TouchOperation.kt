package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.error.MemcachedError.ClientError
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.TouchResult

internal class TouchOperation(
    private val key: String,
    private val expiration: Expiration,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, TouchResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): TouchResult {
        val result = when (val result = GetsOperation(key).run(socket)) {
            is GetsGatsResult.Value<ByteArray> -> {
                when (CasOperation(key, result.flags, expiration, result.data, result.casUnique, reply).run(socket)) {
                    is CasResult.Stored -> TouchResult.Touched
                    is CasResult.NotFound -> TouchResult.NotFound
                    else -> throw ClientError("Item changed while trying to touch").asException()
                }
            }

            else -> TouchResult.NotFound
        }

        return if (reply == Reply.NO_REPLY) {
            TouchResult.NoReply
        } else {
            result
        }
    }
}
