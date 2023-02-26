package com.raycoarana.memkched.internal.text.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.DeleteResult.Deleted
import com.raycoarana.memkched.internal.result.DeleteResult.NoReply
import com.raycoarana.memkched.internal.result.DeleteResult.NotFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DeleteOperationUnitTest : BaseOperationUnitTest<DeleteResult>() {
    @Test
    fun `delete key with reply when value exists`() {
        givenOperation(DeleteOperation(SOME_KEY, Reply.DEFAULT))
        expectWrittenLine("delete $SOME_KEY")
        givenReadLineReturns("DELETED")

        whenRun()

        thenOperationResultIs(Deleted)
    }

    @Test
    fun `delete key with reply when value not exists`() {
        givenOperation(DeleteOperation(SOME_KEY, Reply.DEFAULT))
        expectWrittenLine("delete $SOME_KEY")
        givenReadLineReturns("NOT_FOUND")

        whenRun()

        thenOperationResultIs(NotFound)
    }

    @Test
    fun `delete key with reply when error`() {
        givenOperation(DeleteOperation(SOME_KEY, Reply.DEFAULT))
        expectWrittenLine("delete $SOME_KEY")
        givenReadLineReturns("SERVER_ERROR we are full man!")

        assertThrows<MemcachedException>("SERVER_ERROR we are full man!") {
            whenRun()
        }
    }

    @Test
    fun `delete key with no-reply`() {
        givenOperation(DeleteOperation(SOME_KEY, Reply.NO_REPLY))
        expectWrittenLine("delete $SOME_KEY noreply")

        whenRun()

        thenOperationResultIs(NoReply)
    }
}
