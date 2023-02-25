package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class GetsOperation(
    private val key: String
) : Operation<TextProtocolSocketChannelWrapper, GetsGatsResult<ByteArray>>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): GetsGatsResult<ByteArray> {
        val cmd = buildCommand()
        socketChannelWrapper.writeLine(cmd)
        val result = socketChannelWrapper.readLine()
        if (result != END) {
            val valueLine = ValueLine.parseValue(result)
            val data = socketChannelWrapper.readBinary(valueLine.bytesCount)
            val endLine = socketChannelWrapper.readLine()
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
