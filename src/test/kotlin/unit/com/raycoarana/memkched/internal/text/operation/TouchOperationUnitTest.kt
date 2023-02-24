package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.TouchResult
import com.raycoarana.memkched.internal.result.TouchResult.NoReply
import com.raycoarana.memkched.internal.result.TouchResult.NotFound
import com.raycoarana.memkched.internal.result.TouchResult.Touched
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TouchOperationUnitTest : BaseOperationUnitTest<TouchResult>() {
    @Test
    fun `touch key with reply when value is stored`() {
        givenOperation(TouchOperation(SOME_KEY, Relative(100), Reply.DEFAULT))
        expectWrittenLine("touch $SOME_KEY 100")
        givenReadLineReturns("TOUCHED")

        whenRun()

        thenOperationResultIs(Touched)
    }

    @Test
    fun `touch key with reply when value is not stored`() {
        givenOperation(TouchOperation(SOME_KEY, Relative(100), Reply.DEFAULT))
        expectWrittenLine("touch $SOME_KEY 100")
        givenReadLineReturns("NOT_FOUND")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `touch key with reply when error`() {
        givenOperation(TouchOperation(SOME_KEY, Relative(100), Reply.DEFAULT))
        expectWrittenLine("touch $SOME_KEY 100")
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `touch key with no-reply when value is stored`() {
        givenOperation(TouchOperation(SOME_KEY, Relative(100), Reply.NO_REPLY))
        expectWrittenLine("touch $SOME_KEY 100 noreply")

        whenRun()

        thenOperationResultIs(NoReply)
    }
}
