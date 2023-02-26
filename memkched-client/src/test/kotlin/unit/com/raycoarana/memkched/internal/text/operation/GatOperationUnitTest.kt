package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GatOperationUnitTest : BaseOperationUnitTest<GetGatResult<ByteArray>>() {
    @Test
    fun `gat key when value is not found`() {
        givenOperation(GatOperation(SOME_KEY, SOME_EXPIRATION))
        expectWrittenLine("gat $SOME_EXPIRATION_VALUE $SOME_KEY")
        givenReadLineReturns("END")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `gat key when value is found`() {
        givenOperation(GatOperation(SOME_KEY, SOME_EXPIRATION))
        expectWrittenLine("gat $SOME_EXPIRATION_VALUE $SOME_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        whenRun()

        thenOperationResultIs(GetGatResult.Value(Flags().set(0), "abcde".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun `fail when gat returns a not matching key`() {
        givenOperation(GatOperation(SOME_KEY, SOME_EXPIRATION))
        expectWrittenLine("gat $SOME_EXPIRATION_VALUE $SOME_KEY")
        givenReadLineReturns("VALUE other-key 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        assertThrows<AssertionError> {
            whenRun()
        }
    }
}
