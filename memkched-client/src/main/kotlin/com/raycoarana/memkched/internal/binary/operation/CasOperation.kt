package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.buildExtrasWith
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.KEY_EXISTS
import com.raycoarana.memkched.internal.binary.model.Status.KEY_NOT_FOUND
import com.raycoarana.memkched.internal.result.CasResult

internal class CasOperation(
    private val key: String,
    private val flags: Flags,
    private val expiration: Expiration,
    private val data: ByteArray,
    private val casUnique: CasUnique,
    private val reply: Reply
) : Operation<BinaryProtocolSocketChannelWrapper, CasResult>() {
    internal override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): CasResult {
        val opCode = if (reply == Reply.NO_REPLY) {
            OpCode.SETQ
        } else {
            OpCode.SET
        }
        socket.writePackage(
            opCode = opCode,
            key = key,
            extras = buildExtrasWith(flags, expiration),
            body = data,
            cas = casUnique.value
        )

        return if (reply == Reply.DEFAULT) {
            socket.readHeader(
                headerProcess = { resultOpCode, _, _, _, _, _ ->
                    assert(resultOpCode == OpCode.SET) { "Unexpected op code in response $resultOpCode" }

                    CasResult.Stored
                },
                errorProcess = { error, _ ->
                    when (error.status) {
                        KEY_EXISTS -> CasResult.Exists
                        KEY_NOT_FOUND -> CasResult.NotFound
                        else -> throw error.asException()
                    }
                }
            )
        } else {
            CasResult.NoReply
        }
    }
}
