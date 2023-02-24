package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.IncrDecrResult.NoReply
import com.raycoarana.memkched.internal.result.IncrDecrResult.NotFound
import com.raycoarana.memkched.internal.result.IncrDecrResult.Value
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class IncrOperationUnitTest : BaseOperationUnitTest<IncrDecrResult>() {
    @Test
    fun `incr key with reply when value exists`() {
        givenOperation(IncrOperation(SOME_KEY, SOME_INCREMENT, Reply.DEFAULT))
        expectWrittenLine("incr $SOME_KEY 100")
        givenReadLineReturns("101")

        whenRun()

        thenOperationResultIs(Value(101.toULong()))
    }

    @Test
    fun `incr key with reply when value does not exists`() {
        givenOperation(IncrOperation(SOME_KEY, SOME_INCREMENT, Reply.DEFAULT))
        expectWrittenLine("incr $SOME_KEY 100")
        givenReadLineReturns("NOT_FOUND")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `incr key with reply when error`() {
        givenOperation(IncrOperation(SOME_KEY, SOME_INCREMENT, Reply.DEFAULT))
        expectWrittenLine("incr $SOME_KEY 100")
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `touch key with no-reply when value is stored`() {
        givenOperation(IncrOperation(SOME_KEY, SOME_INCREMENT, Reply.NO_REPLY))
        expectWrittenLine("incr $SOME_KEY 100 noreply")

        whenRun()

        thenOperationResultIs(NoReply)
    }

    companion object {
        private val SOME_INCREMENT = 100L.toULong()
    }
}
