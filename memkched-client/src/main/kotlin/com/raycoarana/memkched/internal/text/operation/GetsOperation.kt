package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class GetsOperation(
    private val key: String
) : Operation<TextProtocolSocketChannelWrapper, GetsGatsResult<ByteArray>>() {
    override suspend fun run(socket: TextProtocolSocketChannelWrapper): GetsGatsResult<ByteArray> {
        val cmd = buildCommand()
        socket.writeLine(cmd)
        val result = socket.readLine()
        if (result != END) {
            val valueLine = ValueLine.parseValue(result)
            val data = socket.readBinary(valueLine.bytesCount)
            val endLine = socket.readLine()
            assert(endLine == END) {
                "Missing END"
            }
            assert(valueLine.key == key) {
                "Unexpected key ${valueLine.key} when requesting $key"
            }
            val casUnique = valueLine.casUnique ?: error("No CAS Unique found")
            return GetsGatsResult.Value(valueLine.flags, data, casUnique)
        }
        return GetsGatsResult.NotFound
    }

    protected open fun buildCommand() = "gets $key"
}
