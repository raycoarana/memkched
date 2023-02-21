package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.text.TextProtocolSocketChannelWrapper
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

internal open class BaseOperationUnitTest<T : Any>(
    private val operation: Operation<TextProtocolSocketChannelWrapper, T>
) {
    private val socketChannel: TextProtocolSocketChannelWrapper = mockk()
    private lateinit var result: T

    protected fun expectWrittenLine(line: String) {
        coEvery { socketChannel.writeLine(line) } just Runs
    }

    protected fun givenReadLineReturns(vararg readLineResult: String) {
        coEvery { socketChannel.readLine() } returnsMany readLineResult.toList()
    }

    protected fun givenReadBinaryBlock(byteArray: ByteArray) {
        coEvery { socketChannel.readBinary(byteArray.size) } returns byteArray
    }

    protected fun whenRun() {
        result = runBlocking {
            operation.execute(socketChannel)
            operation.await(1000)
        }
    }

    protected fun thenOperationResultIs(expected: T) {
        assertEquals(expected, result)
    }

    companion object {
        const val SOME_KEY = "some-key"
    }
}
