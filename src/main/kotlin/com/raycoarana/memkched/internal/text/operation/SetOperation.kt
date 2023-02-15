package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.SetResult.Stored
import com.raycoarana.memkched.internal.text.CLIENT_ERROR
import com.raycoarana.memkched.internal.text.EOL
import com.raycoarana.memkched.internal.text.ERROR
import com.raycoarana.memkched.internal.text.SERVER_ERROR
import com.raycoarana.memkched.internal.text.STORED
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import org.slf4j.LoggerFactory

internal class SetOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val replay: Reply
) : Operation<TextProtocolSocketChannelWrapper, SetResult>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): SetResult {
        val cmd = "set $key ${flags.toUShort()} ${expiration.value} ${data.size}${replay.asTextCommandValue()}"
        socketChannelWrapper.writeLine(cmd)
        socketChannelWrapper.writeBinary(data)
        val result = socketChannelWrapper.readLine()
        return when {
            result == STORED -> Stored
            result == ERROR -> SetResult.Error
            result.startsWith(CLIENT_ERROR) -> SetResult.ClientError(result.substring(CLIENT_ERROR.length + 1))
            result.startsWith(SERVER_ERROR) -> SetResult.ServerError(result.substring(SERVER_ERROR.length + 1))
            else -> {
                logger.error("Unexpected response received: $result")
                SetResult.Error
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SetOperation::class.java)
    }
}
