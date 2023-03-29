package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal class GatOperation(
    private val key: String,
    private val expiration: Expiration
) : Operation<BinaryProtocolSocketChannelWrapper, GetGatResult<ByteArray>>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): GetGatResult<ByteArray> =
        when (val getResult = GetsOperation(key).run(socket)) {
            is GetsGatsResult.Value<ByteArray> -> CasOperation(
                key,
                getResult.flags,
                expiration,
                getResult.data,
                getResult.casUnique,
                Reply.NO_REPLY
            ).run(socket).let { GetGatResult.Value(getResult.flags, getResult.data) }

            else -> GetGatResult.NotFound
        }
}
