package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.text.EOL
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper

class SetOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val replay: Reply = Reply.DEFAULT
) : Operation<TextProtocolSocketChannelWrapper, SetResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): SetResult {
        val cmd = "set $key ${flags.toUShort()} ${expiration.value} ${data.size}${replay.asTextCommandValue()}$EOL"
        socketChannelWrapper.writeLine(cmd)
        socketChannelWrapper.writeBinary(data)
        val result = socketChannelWrapper.readLine()
        when {
            result == "STORED" -> TODO()
            result == "ERROR" -> TODO("fatal error, should never happen")
            result.startsWith("CLIENT_ERROR") -> TODO("extract and throw with message")
            result.startsWith("SERVER_ERROR") -> TODO("extract and throw with message")
        }

        return SetResult(success)
    }
}
