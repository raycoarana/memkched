package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.OpCode
import com.raycoarana.memkched.internal.binary.Status.KEY_NOT_FOUND
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.Value
import java.nio.ByteBuffer

internal open class GetOperation(
    private val key: String
) : Operation<BinaryProtocolSocketChannelWrapper, GetGatResult<ByteArray>>() {
    override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): GetGatResult<ByteArray> {
        socket.writePackage(OpCode.GET, key = key)

        return socket.readHeader(
            { opCode, keyLength, extrasLength, totalBodyLength, _, _ ->
                require(opCode == OpCode.GET) { "Unexpected opCode $opCode" }

                socket.readValue(extrasLength, keyLength, totalBodyLength)
            },
            { error, key ->
                key?.let { require(it == key) { "Unexpected key $it"} }

                if (error.status == KEY_NOT_FOUND) {
                    GetGatResult.NotFound
                } else {
                    throw error.asException()
                }
            }
        )
    }

    private suspend fun BinaryProtocolSocketChannelWrapper.readValue(
        extrasLength: Byte,
        keyLength: Short,
        totalBodyLength: Int
    ): Value<ByteArray> {
        val extras = readBinary(extrasLength.toInt())
        if (keyLength > 0) {
            val responseKey = String(readBinary(keyLength.toInt()), Charsets.US_ASCII)

            require(responseKey == key) { "Unexpected response key $responseKey" }
        }

        val body = readBinary(totalBodyLength - extrasLength - keyLength)
        val flags = Flags.from(ByteBuffer.wrap(extras).getShort().toUShort())

        return Value(flags, body)
    }
}
