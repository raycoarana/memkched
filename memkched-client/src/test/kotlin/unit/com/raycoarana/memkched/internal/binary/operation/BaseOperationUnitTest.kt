package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.binary.BinaryProtocolSocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

internal open class BaseOperationUnitTest<T : Any> {
    private val socketChannel: BinaryProtocolSocketChannelWrapper = mockk()
    private lateinit var operation: Operation<BinaryProtocolSocketChannelWrapper, T>
    private lateinit var result: T

    protected fun givenOperation(value: Operation<BinaryProtocolSocketChannelWrapper, T>) {
        operation = value
    }

    protected fun expectHeaderSent(
        opCode: OpCode,
        cas: Long = 0L,
        key: String? = null,
        extras: ByteArray? = null,
        body: ByteArray? = null
    ) {
        coEvery { socketChannel.writePackage(opCode, cas = cas, key = key, extras = extras, body = body) } just Runs
    }

    protected fun expectSuccessHeaderReceived(
        opCode: OpCode,
        keyLength: Short = 0,
        extrasLength: Byte = 0,
        totalBodyLength: Int = 0,
        opaque: Int = 0,
        cas: Long = 0
    ) {
        coEvery {
            socketChannel.readHeader<Any>(
                headerProcess = any(),
                errorProcess = any()
            )
        } coAnswers {
            val headerProcess: suspend (OpCode, Short, Byte, Int, Int, Long) -> Any = firstArg()
            headerProcess.invoke(opCode, keyLength, extrasLength, totalBodyLength, opaque, cas)
        }
    }

    protected fun expectErrorHeaderReceived(
        error: BinaryProtocolError,
        key: String? = null
    ) {
        coEvery {
            socketChannel.readHeader<Any>(
                headerProcess = any(),
                errorProcess = any()
            )
        } coAnswers {
            val errorProcess: suspend (BinaryProtocolError, String?) -> Any = secondArg()
            errorProcess.invoke(error, key)
        }
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
