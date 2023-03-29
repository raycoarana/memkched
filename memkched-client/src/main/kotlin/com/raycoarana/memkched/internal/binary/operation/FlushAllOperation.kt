package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.buildExtrasWith
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.result.FlushAllResult

internal class FlushAllOperation(
    private val expiration: Expiration?,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, FlushAllResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): FlushAllResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.FLUSHQ
        } else {
            OpCode.FLUSH
        }
        socket.writePackage(
            opCode = opCode,
            extras = expiration?.let { buildExtrasWith(expiration = it) },
        )

        return if (reply == Reply.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.FLUSH) { "Unexpected op code in response $resultOpCode" }

                    FlushAllResult.Ok
                },
                errorProcess = { error, _ ->
                    when (error.status) {
                        else -> throw error.asException()
                    }
                }
            )
        } else {
            FlushAllResult.NoReply
        }
    }
}
