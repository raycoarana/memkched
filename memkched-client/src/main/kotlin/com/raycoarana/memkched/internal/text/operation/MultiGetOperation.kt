package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class MultiGetOperation(
    private val keys: List<String>
) : Operation<TextProtocolSocketChannelWrapper, Map<String, GetGatResult<ByteArray>>>() {
    override suspend fun run(
        socket: TextProtocolSocketChannelWrapper
    ): Map<String, GetGatResult<ByteArray>> {
        if (keys.isEmpty()) {
            return emptyMap()
        }

        val cmd = keys.joinToString(separator = " ", prefix = buildCommandPrefix())
        socket.writeLine(cmd)
        var endLineCandidate = socket.readLine()
        val resultMap = HashMap<String, GetGatResult.Value<ByteArray>>()
        while (endLineCandidate != END) {
            val result = endLineCandidate

            val valueLine = ValueLine.parseValue(result)
            val data = socket.readBinary(valueLine.bytesCount)
            endLineCandidate = socket.readLine()
            resultMap[valueLine.key] = GetGatResult.Value(valueLine.flags, data)
        }

        return keys.associateBy({ key -> key }) { resultMap.getOrDefault(it, NotFound) }
    }

    protected open fun buildCommandPrefix() = "get "
}
