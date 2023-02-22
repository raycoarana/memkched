package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.SetResult.NoReply
import com.raycoarana.memkched.internal.result.SetResult.Stored
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SetOperationUnitTest : BaseOperationUnitTest<SetResult>() {
    @Test
    fun `set key with replay when value is stored`() {
        givenOperation(SetOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("set $SOME_KEY 0 100 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("STORED")

        whenRun()

        thenOperationResultIs(Stored)
    }

    @Test
    fun `set key with replay when value is error`() {
        givenOperation(SetOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.DEFAULT))
        expectWrittenLine("set $SOME_KEY 0 100 5")
        expectWrittenBinaryBlock(DATA_BLOCK)
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `set key with no-replay when value is stored`() {
        givenOperation(SetOperation(SOME_KEY, Flags(), Relative(100), DATA_BLOCK, Reply.NO_REPLY))
        expectWrittenLine("set $SOME_KEY 0 100 5 noreply")
        expectWrittenBinaryBlock(DATA_BLOCK)

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val DATA_BLOCK = "abcde".toByteArray(Charsets.US_ASCII)
    }
}
