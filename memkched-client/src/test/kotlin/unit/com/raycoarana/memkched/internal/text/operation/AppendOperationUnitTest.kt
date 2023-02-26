package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.AppendPrependResult.NoReply
import com.raycoarana.memkched.internal.result.AppendPrependResult.NotStored
import com.raycoarana.memkched.internal.result.AppendPrependResult.Stored
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AppendOperationUnitTest : BaseOperationUnitTest<AppendPrependResult>() {
    @Test
    fun `append key with reply when value already exists`() {
        givenOperation(AppendOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("append $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("STORED")

        whenRun()

        thenOperationResultIs(Stored)
    }

    @Test
    fun `append key with reply when value not exists`() {
        givenOperation(AppendOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("append $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("NOT_STORED")

        whenRun()

        thenOperationResultIs(NotStored)
    }

    @Test
    fun `append key with reply when value is error`() {
        givenOperation(AppendOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("append $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `set key with no-reply when value is stored`() {
        givenOperation(AppendOperation(SOME_KEY, DATA_BLOCK, Reply.NO_REPLY))
        expectWrittenLine("append $SOME_KEY 0 0 5 noreply")
        expectWrittenBinaryBlock(DATA_BLOCK)

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val DATA_BLOCK = "abcde".toByteArray(Charsets.US_ASCII)
    }
}
