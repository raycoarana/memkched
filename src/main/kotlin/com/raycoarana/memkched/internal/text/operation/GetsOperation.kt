package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal class GetsOperation(
    private val key: String
) : Operation<TextProtocolSocketChannelWrapper, GetsResult<ByteArray>>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): GetsResult<ByteArray> {
        val cmd = "gets $key"
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
            return GetsResult.Value(valueLine.flags, data, casUnique)
        }
        return GetsResult.NotFound
    }
}
