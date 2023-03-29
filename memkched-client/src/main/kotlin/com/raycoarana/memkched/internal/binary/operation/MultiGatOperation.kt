package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal open class MultiGatOperation(
    private val keys: List<String>,
    private val expiration: Expiration
) : Operation<BinaryProtocolSocketChannelWrapper, Map<String, GetGatResult<ByteArray>>>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): Map<String, GetGatResult<ByteArray>> =
        MultiGetsOperation(keys).run(socket).mapValues {
            val value = it.value
            if (value is GetsGatsResult.Value<ByteArray>) {
                CasOperation(it.key, value.flags, expiration, value.data, value.casUnique, Reply.NO_REPLY).run(socket)
                GetGatResult.Value(value.flags, value.data)
            } else {
                GetGatResult.NotFound
            }
        }
}
