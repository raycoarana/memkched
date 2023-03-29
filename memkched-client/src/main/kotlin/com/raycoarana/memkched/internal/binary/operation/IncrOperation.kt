package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Expiration.Absolute
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.buildExtrasWith
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.KEY_NOT_FOUND
import com.raycoarana.memkched.internal.result.IncrDecrResult

internal class IncrOperation(
    private val key: String,
    private val value: ULong,
    private val reply: Reply,
    private val initialValue: ULong = 0.toULong(),
    private val expiration: Expiration = Absolute.MAX_VALUE
) : Operation<BinaryProtocolSocketChannelWrapper, IncrDecrResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): IncrDecrResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.INCREMENTQ
        } else {
            OpCode.INCREMENT
        }
        socket.writePackage(
            opCode = opCode,
            key = key,
            extras = buildExtrasWith(value, initialValue, expiration),
        )

        return if (reply == Reply.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.INCREMENT) { "Unexpected op code in response $resultOpCode" }

                    IncrDecrResult.Value(socket.readULong())
                },
                errorProcess = { error, _ ->
                    when (error.status) {
                        KEY_NOT_FOUND -> IncrDecrResult.NotFound
                        else -> throw error.asException()
                    }
                }
            )
        } else {
            IncrDecrResult.NoReply
        }
    }
}
