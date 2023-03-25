package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.OpCode
import com.raycoarana.memkched.internal.binary.Status.KEY_NOT_FOUND
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.Value
import java.nio.ByteBuffer

internal open class MultiGetOperation(
    private val keys: List<String>
) : Operation<BinaryProtocolSocketChannelWrapper, Map<String, GetGatResult<ByteArray>>>() {
    override suspend fun run(
        socket: BinaryProtocolSocketChannelWrapper
    ): Map<String, GetGatResult<ByteArray>> {
        val lastIndex = keys.size - 1
        keys.forEachIndexed { index, key ->
            if (index != lastIndex) {
                socket.writePackage(OpCode.GETKQ, key = key)
            } else {
                socket.writePackage(OpCode.GETK, key = key)
            }
        }

        val result: MutableMap<String, GetGatResult<ByteArray>> = keys.associateWith { GetGatResult.NotFound }
            .toMutableMap()
        do {
            val readKey = socket.readHeader(
                { opCode, keyLength, extrasLength, totalBodyLength, _, _ ->
                    require(opCode == OpCode.GETK || opCode == OpCode.GETKQ) { "Unexpected opCode $opCode" }

                    val (key, value) = socket.readValue(extrasLength, keyLength, totalBodyLength)
                    result[key] = value
                    key
                },
                { error, key ->
                    if (error.status == KEY_NOT_FOUND) {
                        result[key!!] = GetGatResult.NotFound
                        key
                    } else {
                        throw error.asException()
                    }
                }
            )
        } while (readKey != keys.last())

        return result
    }

    private suspend fun BinaryProtocolSocketChannelWrapper.readValue(
        extrasLength: Byte,
        keyLength: Short,
        totalBodyLength: Int
    ): Pair<String, Value<ByteArray>> {
        val extras = readBinary(extrasLength.toInt())
        val responseKey = String(readBinary(keyLength.toInt()), Charsets.US_ASCII)

        val body = readBinary(totalBodyLength - extrasLength - keyLength)
        val flags = Flags.from(ByteBuffer.wrap(extras).getShort().toUShort())

        return responseKey to Value(flags, body)
    }
}
