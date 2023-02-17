package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetResult.NotFound
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal class MultiGetOperation(
    private val keys: List<String>
) : Operation<TextProtocolSocketChannelWrapper, Map<String, GetResult<ByteArray>>>() {
    override suspend fun run(socketChannelWrapper: TextProtocolSocketChannelWrapper): Map<String, GetResult<ByteArray>> {
        val cmd = keys.joinToString(separator = " ", prefix = "get ")
        socketChannelWrapper.writeLine(cmd)
        var endLineCandidate = socketChannelWrapper.readLine()
        val resultMap = HashMap<String, GetResult.Value<ByteArray>>()
        while (endLineCandidate != END) {
            val result = endLineCandidate

            val valueLine = ValueLine.parseValue(result)
            val data = socketChannelWrapper.readBinary(valueLine.bytesCount)
            endLineCandidate = socketChannelWrapper.readLine()
            resultMap[valueLine.key] = GetResult.Value(valueLine.flags, data)
        }

        return keys.associateBy({ key -> key }) { resultMap.getOrDefault(it, NotFound) }
    }
}
