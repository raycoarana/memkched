package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.result.GetsResult.NotFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MultiGetsOperationUnitTest : BaseOperationUnitTest<Map<String, GetsResult<ByteArray>>>() {
    @Test
    fun `gets key when value is not found`() {
        givenOperation(MultiGetsOperation(listOf(SOME_KEY, SOME_OTHER_KEY)))
        expectWrittenLine("gets $SOME_KEY $SOME_OTHER_KEY")
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
    fun `gets key when one value is found`() {
        givenOperation(MultiGetsOperation(listOf(SOME_KEY, SOME_OTHER_KEY)))
        expectWrittenLine("gets $SOME_KEY $SOME_OTHER_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5 27", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        whenRun()

        thenOperationResultIs(
            mapOf(
                SOME_KEY to GetsResult.Value(Flags().set(0), "abcde".toByteArray(Charsets.US_ASCII), CasUnique(27)),
                SOME_OTHER_KEY to NotFound
            )
        )
    }

    @Test
    fun `gets no key will return empty map`() {
        givenOperation(MultiGetsOperation(emptyList()))

        whenRun()

        thenOperationResultIs(emptyMap())
    }

    @Test
    fun `fail when gets returns not include cas unique`() {
        givenOperation(MultiGetsOperation(listOf(SOME_KEY)))
        expectWrittenLine("gets $SOME_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        assertThrows<IllegalStateException> {
            whenRun()
        }
    }
}
