package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status.KEY_NOT_FOUND
import java.nio.ByteBuffer

internal abstract class AbstractMultiGetOperation<T>(
    private val keys: List<String>
) : Operation<BinaryProtocolSocketChannelWrapper, Map<String, T>>() {
    override suspend fun run(
        socket: BinaryProtocolSocketChannelWrapper
    ): Map<String, T> {
        val lastIndex = keys.size - 1
        keys.forEachIndexed { index, key ->
            if (index != lastIndex) {
                socket.writePackage(OpCode.GETKQ, key = key)
            } else {
                socket.writePackage(OpCode.GETK, key = key)
            }
        }

        val notFound = mapToNotFound()
        val result: MutableMap<String, T> = keys.associateWith { notFound }
            .toMutableMap()
        do {
            val readKey = socket.readHeader(
                { opCode, keyLength, extrasLength, totalBodyLength, _, cas ->
                    require(opCode == OpCode.GETK || opCode == OpCode.GETKQ) { "Unexpected opCode $opCode" }

                    val (key, value) = socket.readValue(extrasLength, keyLength, totalBodyLength, cas)
                    result[key] = value
                    key
                },
                { error, key ->
                    if (error.status == KEY_NOT_FOUND) {
                        result[key!!] = notFound
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
        totalBodyLength: Int,
        cas: Long
    ): Pair<String, T> {
        val extras = readBinary(extrasLength.toInt())
        val responseKey = String(readBinary(keyLength.toInt()), Charsets.US_ASCII)

        val value = readBinary(totalBodyLength - extrasLength - keyLength)
        val flags = Flags.from(ByteBuffer.wrap(extras).getShort().toUShort())

        return responseKey to mapToValue(flags, value, CasUnique(cas))
    }

    protected abstract fun mapToValue(flags: Flags, value: ByteArray, casUnique: CasUnique): T
    protected abstract fun mapToNotFound(): T
}
