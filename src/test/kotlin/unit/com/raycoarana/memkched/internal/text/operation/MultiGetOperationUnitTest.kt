package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetResult.NotFound
import org.junit.jupiter.api.Test

internal class MultiGetOperationUnitTest : BaseOperationUnitTest<Map<String, GetResult<ByteArray>>>() {
    @Test
    fun `get key when value is not found`() {
        givenOperation(MultiGetOperation(listOf(SOME_KEY, SOME_OTHER_KEY)))
        expectWrittenLine("get $SOME_KEY $SOME_OTHER_KEY")
        givenReadLineReturns("END")

        whenRun()

        thenOperationResultIs(
            mapOf(
                SOME_KEY to NotFound,
                SOME_OTHER_KEY to NotFound
            )
        )
    }

    @Test
    fun `get key when one value is found`() {
        givenOperation(MultiGetOperation(listOf(SOME_KEY, SOME_OTHER_KEY)))
        expectWrittenLine("get $SOME_KEY $SOME_OTHER_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        whenRun()

        thenOperationResultIs(
            mapOf(
                SOME_KEY to GetResult.Value(Flags().set(0), "abcde".toByteArray(Charsets.US_ASCII)),
                SOME_OTHER_KEY to NotFound
            )
        )
    }

    @Test
    fun `get no key will return empty map`() {
        givenOperation(MultiGetOperation(emptyList()))

        whenRun()

        thenOperationResultIs(emptyMap())
    }
}
