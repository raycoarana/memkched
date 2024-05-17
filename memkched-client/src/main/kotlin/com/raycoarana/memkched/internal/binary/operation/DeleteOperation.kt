package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.KEY_NOT_FOUND
import com.raycoarana.memkched.internal.result.DeleteResult

internal class DeleteOperation(
    private val key: String,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, DeleteResult>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): DeleteResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.DELETEQ
        } else {
            OpCode.DELETE
        }
        socket.writePackage(
            opCode = opCode,
            key = key,
        )

        return if (reply == Reply.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.DELETE) { "Unexpected op code in response $resultOpCode" }

                    DeleteResult.Deleted
                },
                errorProcess = { error, _ ->
                    when (error.status) {
                        KEY_NOT_FOUND -> DeleteResult.NotFound
                        else -> throw error.asException()
                    }
                }
            )
        } else {
            DeleteResult.NoReply
        }
    }
}
