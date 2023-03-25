package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal open class MultiGetsOperation(
    private val keys: List<String>
) : Operation<BinaryProtocolSocketChannelWrapper, Map<String, GetsGatsResult<ByteArray>>>() {
    override suspend fun run(
        socketChannelWrapper: BinaryProtocolSocketChannelWrapper
    ): Map<String, GetsGatsResult<ByteArray>> {
        TODO("Not yet implemented")
    }
}
