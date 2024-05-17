package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.KEY_NOT_FOUND
import java.nio.ByteBuffer

internal abstract class AbstractGetOperation<T>(
    private val opCode: OpCode,
    private val key: String
) : Operation<BinaryProtocolSocketChannelWrapper, T>() {
    internal override suspend fun run(socket: BinaryProtocolSocketChannelWrapper): T {
        socket.writePackage(opCode, key = key)

        return socket.readHeader(
            { opCode, keyLength, extrasLength, totalBodyLength, _, cas ->
                require(opCode == OpCode.GET) { "Unexpected opCode $opCode" }

                socket.readSuccess(extrasLength, keyLength, totalBodyLength, cas)
            },
            { error, key ->
                key?.let { require(it == key) { "Unexpected key $it" } }

                if (error.status == KEY_NOT_FOUND) {
                    mapToNotFound()
                } else {
                    throw error.asException()
                }
            }
        )
    }

    private suspend fun BinaryProtocolSocketChannelWrapper.readSuccess(
        extrasLength: Byte,
        keyLength: Short,
        totalBodyLength: Int,
        cas: Long
    ): T {
        val flags = Flags.from(
            ByteBuffer.wrap(readBinary(extrasLength.toInt())).getShort().toUShort()
        )

        if (keyLength > 0) {
            val responseKey = String(readBinary(keyLength.toInt()), Charsets.US_ASCII)

            require(responseKey == key) { "Unexpected response key $responseKey" }
        }

        val value = readBinary(totalBodyLength - extrasLength - keyLength)

        return mapToValue(flags, value, CasUnique(cas))
    }

    protected abstract fun mapToValue(flags: Flags, value: ByteArray, cas: CasUnique): T
    protected abstract fun mapToNotFound(): T
}
