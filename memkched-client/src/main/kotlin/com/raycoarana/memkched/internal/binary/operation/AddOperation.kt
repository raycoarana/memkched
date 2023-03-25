package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.AddReplaceResult

internal class AddOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, AddReplaceResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): AddReplaceResult {
        TODO("Not yet implemented")
    }
}
