package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import com.raycoarana.memkched.internal.text.END
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.text.parsing.ValueLine

internal open class MultiGetOperation(
    private val keys: List<String>
) : Operation<TextProtocolSocketChannelWrapper, Map<String, GetGatResult<ByteArray>>>(),
    TextOperation<Map<String, GetGatResult<ByteArray>>> {
    override val readsResponse: Boolean
        get() = keys.isNotEmpty()

    override suspend fun writeRequest(socketChannelWrapper: TextProtocolSocketChannelWrapper) {
        if (keys.isNotEmpty()) {
            socketChannelWrapper.writeLine(keys.joinToString(separator = " ", prefix = buildCommandPrefix()))
        }
    }

    override suspend fun readResponse(
        socketChannelWrapper: TextProtocolSocketChannelWrapper
    ): Map<String, GetGatResult<ByteArray>> {
        var endLineCandidate = socketChannelWrapper.readLine()
        val resultMap = HashMap<String, GetGatResult.Value<ByteArray>>()
        while (endLineCandidate != END) {
            val result = endLineCandidate

            val valueLine = ValueLine.parseValue(result)
            val data = socketChannelWrapper.readBinary(valueLine.bytesCount)
            endLineCandidate = socketChannelWrapper.readLine()
            resultMap[valueLine.key] = GetGatResult.Value(valueLine.flags, data)
        }

        return keys.associateBy({ key -> key }) { resultMap.getOrDefault(it, NotFound) }
    }

    override fun noReplyResult(): Map<String, GetGatResult<ByteArray>> = emptyMap()

    protected open fun buildCommandPrefix() = "get "
}
