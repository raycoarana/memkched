package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.AppendPrependResult.NoReply
import com.raycoarana.memkched.internal.result.AppendPrependResult.NotStored
import com.raycoarana.memkched.internal.result.AppendPrependResult.Stored
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PrependOperationUnitTest : BaseOperationUnitTest<AppendPrependResult>() {
    @Test
    fun `prepend key with reply when value already exists`() {
        givenOperation(PrependOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("prepend $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("STORED")

        whenRun()

        thenOperationResultIs(Stored)
    }

    @Test
    fun `prepend key with reply when value not exists`() {
        givenOperation(PrependOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("prepend $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("NOT_STORED")

        whenRun()

        thenOperationResultIs(NotStored)
    }

    @Test
    fun `prepend key with reply when value is error`() {
        givenOperation(PrependOperation(SOME_KEY, DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("prepend $SOME_KEY 0 0 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `set key with no-reply when value is stored`() {
        givenOperation(PrependOperation(SOME_KEY, DATA_BLOCK, Reply.NO_REPLY))
        expectWrittenLine("prepend $SOME_KEY 0 0 5 noreply")
        expectWrittenBinaryBlock(DATA_BLOCK)

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val DATA_BLOCK = "abcde".toByteArray(Charsets.US_ASCII)
    }
}
