package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.FlushAllResult
import com.raycoarana.memkched.internal.result.FlushAllResult.NoReply
import com.raycoarana.memkched.internal.result.FlushAllResult.Ok
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FlushAllOperationUnitTest : BaseOperationUnitTest<FlushAllResult>() {
    @Test
    fun `flush all with reply success`() {
        givenOperation(FlushAllOperation(Relative(100), Reply.DEFAULT))
        expectWrittenLine("flush_all 100")
        givenReadLineReturns("OK")

        whenRun()

        thenOperationResultIs(Ok)
    }

    @Test
    fun `flush all key with reply when error`() {
        givenOperation(FlushAllOperation(expiration = null, Reply.DEFAULT))
        expectWrittenLine("flush_all")
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `flush all with no-reply`() {
        givenOperation(FlushAllOperation(Relative(100), Reply.NO_REPLY))
        expectWrittenLine("flush_all 100 noreply")

        whenRun()

        thenOperationResultIs(NoReply)
    }
}
