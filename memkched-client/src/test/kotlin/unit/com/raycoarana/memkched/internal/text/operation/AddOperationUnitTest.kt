package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.AddReplaceResult.NoReply
import com.raycoarana.memkched.internal.result.AddReplaceResult.NotStored
import com.raycoarana.memkched.internal.result.AddReplaceResult.Stored
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AddOperationUnitTest : BaseOperationUnitTest<AddReplaceResult>() {
    @Test
    fun `add not existing key with reply then value is stored`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("add $SOME_KEY 0 100 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("STORED")

        whenRun()

        thenOperationResultIs(Stored)
    }

    @Test
    fun `add existing key with reply then value is not stored`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("add $SOME_KEY 0 100 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("NOT_STORED")

        whenRun()

        thenOperationResultIs(NotStored)
    }

    @Test
    fun `add key with reply when value is error`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("add $SOME_KEY 0 100 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `add key with no-reply when value is stored`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.NO_REPLY))
        expectWrittenLine("add $SOME_KEY 0 100 5 noreply")
        expectWrittenBinaryBlock(DATA_BLOCK)

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val DATA_BLOCK = "abcde".toByteArray(Charsets.US_ASCII)
    }
}
