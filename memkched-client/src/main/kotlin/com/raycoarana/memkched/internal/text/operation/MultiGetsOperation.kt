package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.NotFound
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class MultiGetsOperation(
    private val keys: List<String>
) : Operation<TextProtocolSocketChannelWrapper, Map<String, GetsGatsResult<ByteArray>>>() {
    override suspend fun run(
        socket: TextProtocolSocketChannelWrapper
    ): Map<String, GetsGatsResult<ByteArray>> {
        if (keys.isEmpty()) {
            return emptyMap()
        }

        val cmd = keys.joinToString(separator = " ", prefix = buildCommandPrefix())
        socket.writeLine(cmd)
        var endLineCandidate = socket.readLine()
        val resultMap = HashMap<String, GetsGatsResult.Value<ByteArray>>()
        while (endLineCandidate != END) {
            val result = endLineCandidate

            val valueLine = ValueLine.parseValue(result)
            val data = socket.readBinary(valueLine.bytesCount)
            endLineCandidate = socket.readLine()
            val casUnique = valueLine.casUnique ?: error("No CAS Unique found")
            resultMap[valueLine.key] = GetsGatsResult.Value(valueLine.flags, data, casUnique)
        }

        return keys.associateBy({ key -> key }) { resultMap.getOrDefault(it, NotFound) }
    }

    protected open fun buildCommandPrefix() = "gets "
}
