package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

internal open class BaseOperationUnitTest<T : Any> {
    private val socketChannel: TextProtocolSocketChannelWrapper = mockk()
    private lateinit var operation: TextOperation<T>
    private lateinit var result: T
    private var expectedLine: String? = null

    protected fun givenOperation(value: Operation<TextProtocolSocketChannelWrapper, T>) {
        operation = value as TextOperation<T>
    }

    protected fun expectWrittenLine(line: String) {
        expectedLine = line
        coEvery { socketChannel.writeLine(line) } just Runs
    }

    protected fun expectWrittenBinaryBlock(byteArray: ByteArray) {
        expectedLine?.let { line ->
            coEvery { socketChannel.writeLineAndBinary(line, byteArray) } just Runs
        }
        coEvery { socketChannel.writeBinary(byteArray) } just Runs
    }

    protected fun givenReadLineReturns(vararg readLineResult: String) {
        coEvery { socketChannel.readLine() } returnsMany readLineResult.toList()
    }

    protected fun givenReadBinaryBlock(byteArray: ByteArray) {
        coEvery { socketChannel.readBinary(byteArray.size) } returns byteArray
    }

    protected fun whenRun() {
        result = runBlocking {
            operation.writeRequest(socketChannel)
            if (operation.readsResponse) {
                operation.readResponse(socketChannel)
            } else {
                operation.noReplyResult()
            }
        }
    }

    protected fun thenOperationResultIs(expected: T) {
        assertEquals(expected, result)
    }

    companion object {
        const val SOME_KEY = "some-key"
        const val SOME_OTHER_KEY = "some-other-key"
        const val SOME_EXPIRATION_VALUE = 100
        val SOME_EXPIRATION = Relative(SOME_EXPIRATION_VALUE)
    }
}
