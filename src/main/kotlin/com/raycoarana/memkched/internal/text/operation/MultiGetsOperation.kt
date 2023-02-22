package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.result.GetsResult.NotFound
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal class MultiGetsOperation(
    private val keys: List<String>
) : Operation<TextProtocolSocketChannelWrapper, Map<String, GetsResult<ByteArray>>>() {
    override suspend fun run(
        socketChannelWrapper: TextProtocolSocketChannelWrapper
    ): Map<String, GetsResult<ByteArray>> {
        if (keys.isEmpty()) {
            return emptyMap()
        }

        val cmd = keys.joinToString(separator = " ", prefix = "gets ")
        socketChannelWrapper.writeLine(cmd)
        var endLineCandidate = socketChannelWrapper.readLine()
        val resultMap = HashMap<String, GetsResult.Value<ByteArray>>()
        while (endLineCandidate != END) {
            val result = endLineCandidate

            val valueLine = ValueLine.parseValue(result)
            val data = socketChannelWrapper.readBinary(valueLine.bytesCount)
            endLineCandidate = socketChannelWrapper.readLine()
            val casUnique = valueLine.casUnique ?: error("No CAS Unique found")
            resultMap[valueLine.key] = GetsResult.Value(valueLine.flags, data, casUnique)
        }

        return keys.associateBy({ key -> key }) { resultMap.getOrDefault(it, NotFound) }
    }
}
