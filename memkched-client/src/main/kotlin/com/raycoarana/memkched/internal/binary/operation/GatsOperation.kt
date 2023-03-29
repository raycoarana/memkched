package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal class GatsOperation(
    private val key: String,
    private val expiration: Expiration
) : Operation<BinaryProtocolSocketChannelWrapper, GetsGatsResult<ByteArray>>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): GetsGatsResult<ByteArray> {
        val result = GetsOperation(key).run(socket)
        if (result is GetsGatsResult.Value<ByteArray>) {
            CasOperation(key, result.flags, expiration, result.data, result.casUnique, Reply.NO_REPLY).run(socket)
        }
        return result
    }
}
