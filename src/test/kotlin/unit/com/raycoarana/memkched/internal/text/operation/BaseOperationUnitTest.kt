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
    private lateinit var operation: Operation<TextProtocolSocketChannelWrapper, T>
    private lateinit var result: T

    protected fun givenOperation(value: Operation<TextProtocolSocketChannelWrapper, T>) {
        operation = value
    }

    protected fun expectWrittenLine(line: String) {
        coEvery { socketChannel.writeLine(line) } just Runs
    }

    protected fun expectWrittenBinaryBlock(byteArray: ByteArray) {
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
            operation.execute(socketChannel)
            operation.await(1000)
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
