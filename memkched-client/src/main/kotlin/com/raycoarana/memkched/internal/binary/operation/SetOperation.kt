package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Reply.Companion
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.buildExtrasWith
import com.raycoarana.memkched.internal.result.SetResult

internal class SetOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, SetResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): SetResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.SETQ
        } else {
            OpCode.SET
        }
        socket.writePackage(
            opCode = opCode,
            key = key,
            extras = buildExtrasWith(flags, expiration),
            body = data
        )

        return if (reply == Companion.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.SET) { "Unexpected op code in response $resultOpCode" }

                    SetResult.Stored
                },
                errorProcess = { error, _ ->
                    throw error.asException()
                }
            )
        } else {
            SetResult.NoReply
        }
    }
}
