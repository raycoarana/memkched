package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.ITEM_NOT_STORED
import com.raycoarana.memkched.internal.result.AppendPrependResult

internal class AppendOperation(
    private val key: String,
    private val data: ByteArray,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, AppendPrependResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): AppendPrependResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.APPENDQ
        } else {
            OpCode.APPEND
        }
        socket.writePackage(
            opCode = opCode,
            key = key,
            body = data
        )

        return if (reply == Reply.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.APPEND) { "Unexpected op code in response $resultOpCode" }

                    AppendPrependResult.Stored
                },
                errorProcess = { error, _ ->
                    when (error.status) {
                        ITEM_NOT_STORED -> AppendPrependResult.NotStored
                        else -> throw error.asException()
                    }
                }
            )
        } else {
            AppendPrependResult.NoReply
        }
    }
}
