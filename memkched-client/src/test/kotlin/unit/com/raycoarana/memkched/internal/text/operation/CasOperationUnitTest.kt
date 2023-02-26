package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.CasResult.Exists
import com.raycoarana.memkched.internal.result.CasResult.NoReply
import com.raycoarana.memkched.internal.result.CasResult.NotFound
import com.raycoarana.memkched.internal.result.CasResult.Stored
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CasOperationUnitTest : BaseOperationUnitTest<CasResult>() {
    @Test
    fun `cas with reply when value is stored`() {
        givenOperation(
            CasOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, CasUnique(123), Reply.DEFAULT)
        )
        expectWrittenLine("cas $SOME_KEY 0 100 5 123")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("STORED")

        whenRun()

        thenOperationResultIs(Stored)
    }

    @Test
    fun `cas with reply when value is not found`() {
        givenOperation(
            CasOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, CasUnique(123), Reply.DEFAULT)
        )
        expectWrittenLine("cas $SOME_KEY 0 100 5 123")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("NOT_FOUND")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `cas with reply when value exists with different cas unique`() {
        givenOperation(
            CasOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, CasUnique(123), Reply.DEFAULT)
        )
        expectWrittenLine("cas $SOME_KEY 0 100 5 123")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("EXISTS")

        whenRun()

        thenOperationResultIs(Exists)
    }

    @Test
    fun `cas with reply when result is error`() {
        givenOperation(CasOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, CasUnique(123), Reply.DEFAULT))
        expectWrittenLine("cas $SOME_KEY 0 100 5 123")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `cas with no-reply`() {
        givenOperation(CasOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, CasUnique(123), Reply.NO_REPLY))
        expectWrittenLine("cas $SOME_KEY 0 100 5 123 noreply")
        expectWrittenBinaryBlock(DATA_BLOCK)

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val DATA_BLOCK = "abcde".toByteArray(Charsets.US_ASCII)
    }
}
