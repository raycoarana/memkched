package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.NotFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MultiGatsOperationUnitTest : BaseOperationUnitTest<Map<String, GetsGatsResult<ByteArray>>>() {
    @Test
    fun `gats key when value is not found`() {
        givenOperation(MultiGatsOperation(listOf(SOME_KEY, SOME_OTHER_KEY), SOME_EXPIRATION))
        expectWrittenLine("gats $SOME_EXPIRATION_VALUE $SOME_KEY $SOME_OTHER_KEY")
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
    fun `gats key when one value is found`() {
        givenOperation(MultiGatsOperation(listOf(SOME_KEY, SOME_OTHER_KEY), SOME_EXPIRATION))
        expectWrittenLine("gats $SOME_EXPIRATION_VALUE $SOME_KEY $SOME_OTHER_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5 27", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        whenRun()

        thenOperationResultIs(
            mapOf(
                SOME_KEY to GetsGatsResult.Value(Flags().set(0), "abcde".toByteArray(Charsets.US_ASCII), CasUnique(27)),
                SOME_OTHER_KEY to NotFound
            )
        )
    }

    @Test
    fun `gats no key will return empty map`() {
        givenOperation(MultiGatsOperation(emptyList(), SOME_EXPIRATION))

        whenRun()

        thenOperationResultIs(emptyMap())
    }

    @Test
    fun `fail when gats returns not include cas unique`() {
        givenOperation(MultiGatsOperation(listOf(SOME_KEY), SOME_EXPIRATION))
        expectWrittenLine("gats $SOME_EXPIRATION_VALUE $SOME_KEY")
        givenReadLineReturns("VALUE $SOME_KEY 1 5", "END")
        givenReadBinaryBlock("abcde".toByteArray(Charsets.US_ASCII))

        assertThrows<IllegalStateException> {
            whenRun()
        }
    }
}
