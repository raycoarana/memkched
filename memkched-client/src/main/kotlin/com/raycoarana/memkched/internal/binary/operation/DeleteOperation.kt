package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.DeleteResult

internal class DeleteOperation(
    private val key: String,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, DeleteResult>() {
    override suspend fun run(socketChannelWrapper: BinaryProtocolSocketChannelWrapper): DeleteResult {
        TODO("Not yet implemented")
    }
}
