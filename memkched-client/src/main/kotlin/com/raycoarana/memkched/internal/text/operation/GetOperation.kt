package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class GetOperation(
    private val key: String
) : Operation<TextProtocolSocketChannelWrapper, GetGatResult<ByteArray>>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): GetGatResult<ByteArray> {
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
            return GetGatResult.Value(valueLine.flags, data)
        }
        return GetGatResult.NotFound
    }

    protected open fun buildCommand() = "get $key"
}
