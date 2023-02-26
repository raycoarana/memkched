package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.NotFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetsOperationUnitTest : BaseOperationUnitTest<GetsGatsResult<ByteArray>>() {
    @Test
    fun `gets key when value is not found`() {
        givenOperation(GetsOperation(SOME_KEY))
        expectWrittenLine("gets $SOME_KEY")
        givenReadLineReturns("END")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `gets key when value is found`() {
        givenOperation(GetsOperation(SOME_KEY))
        expectWrittenLine("gets $SOME_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5 27", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        whenRun()

        thenOperationResultIs(
            GetsGatsResult.Value(Flags().set(0), "abcde".toByteArray(Charsets.US_ASCII), CasUnique(27))
        )
    }

    @Test
    fun `fail when gets returns a not matching key`() {
        givenOperation(GetsOperation(SOME_KEY))
        expectWrittenLine("gets $SOME_KEY")
        givenReadLineReturns("VALUE other-key 1 5 27", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        assertThrows<AssertionError> {
            whenRun()
        }
    }

    @Test
    fun `fail when gets returns not include cas unique`() {
        givenOperation(GetsOperation(SOME_KEY))
        expectWrittenLine("gets $SOME_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        assertThrows<IllegalStateException> {
            whenRun()
        }
    }
}
