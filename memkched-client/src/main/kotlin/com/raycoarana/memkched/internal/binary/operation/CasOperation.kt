package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.result.CasResult

internal class CasOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val casUnique: CasUnique,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, CasResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): CasResult {
        TODO("Not yet implemented")
    }
}
