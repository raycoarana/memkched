package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.error.MemcachedError
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.text.EXISTS
import com.raycoarana.memkched.internal.text.NOT_FOUND
import com.raycoarana.memkched.internal.text.STORED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

internal class CasOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val casUnique: CasUnique,
    private val reply: Reply
) : Operation<TextProtocolSocketChannelWrapper, CasResult>(), TextOperation<CasResult> {
    override val readsResponse: Boolean
        get() = reply != Reply.NO_REPLY

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        socketChannelWrapper.writeLineAndBinary(command(), data)
    }

    override suspend fun readResponse(socketChannelWrapper: TextProtocolSocketChannelWrapper): CasResult =
        when (val result = socketChannelWrapper.readLine()) {
            STORED -> CasResult.Stored
            EXISTS -> CasResult.Exists
            NOT_FOUND -> CasResult.NotFound
            else -> throw MemcachedError.parse(result).asException()
        }

    override fun noReplyResult() = CasResult.NoReply

    private fun command(): String {
        val replyText = reply.asTextCommandValue()
        return "cas $key ${flags.toUShort()} ${expiration.value} ${data.size} ${casUnique.value}$replyText"
    }
}
