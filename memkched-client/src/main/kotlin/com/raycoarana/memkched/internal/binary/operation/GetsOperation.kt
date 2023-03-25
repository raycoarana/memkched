package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.GetsGatsResult

internal open class GetsOperation(
    private val key: String
) : Operation<BinaryProtocolSocketChannelWrapper, GetsGatsResult<ByteArray>>() {
    override suspend fun run(socketChannelWrapper: BinaryProtocolSocketChannelWrapper): GetsGatsResult<ByteArray> {
        TODO("Not yet implemented")
    }
}
