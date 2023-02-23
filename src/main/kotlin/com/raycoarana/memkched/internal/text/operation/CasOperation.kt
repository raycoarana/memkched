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
) : Operation<TextProtocolSocketChannelWrapper, CasResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): CasResult {
        val replyText = reply.asTextCommandValue()
        val cmd = "cas $key ${flags.toUShort()} ${expiration.value} ${data.size} ${casUnique.value}$replyText"
        socketChannelWrapper.writeLine(cmd)
        socketChannelWrapper.writeBinary(data)

        if (reply == Reply.NO_REPLY) {
            return CasResult.NoReply
        }

        return when (val result = socketChannelWrapper.readLine()) {
            STORED -> CasResult.Stored
            EXISTS -> CasResult.Exists
            NOT_FOUND -> CasResult.NotFound
            else -> throw MemcachedError.parse(result).asException()
        }
    }
}
