package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal open class MultiGatsOperation(
    private val keys: List<String>,
    private val expiration: Expiration
) : Operation<BinaryProtocolSocketChannelWrapper, Map<String, GetsGatsResult<ByteArray>>>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): Map<String, GetsGatsResult<ByteArray>> {
        val result = MultiGetsOperation(keys).run(socket)
        result.entries.forEach {
            val value = it.value
            if (value is GetsGatsResult.Value<ByteArray>) {
                CasOperation(it.key, value.flags, expiration, value.data, value.casUnique, Reply.NO_REPLY).run(socket)
            }
        }
        return result
    }
}
