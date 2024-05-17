package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.binary.model.OpCode.PREPEND
import com.raycoarana.memkched.internal.binary.model.OpCode.PREPENDQ
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import com.raycoarana.memkched.internal.result.AppendPrependResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class PrependOperationUnitTest : BaseOperationUnitTest<AppendPrependResult>() {
    @Test
    fun `prepend value when is stored`() {
        givenOperation(PrependOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = PREPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectSuccessHeaderReceived(opCode = PREPEND)

        whenRun()

        thenOperationResultIs(AppendPrependResult.Stored)
    }

    @Test
    fun `prepend value when item not stored`() {
        givenOperation(PrependOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = PREPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(PREPEND, Status.ITEM_NOT_STORED, "error"))

        whenRun()

        thenOperationResultIs(AppendPrependResult.NotStored)
    }

    @Test
    fun `prepend value when unknown error`() {
        givenOperation(PrependOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = PREPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(PREPEND, Status.UNKNOWN, "my-error"))

        val reason = assertThrows<MemcachedException> {
            whenRun()
        }.reason

        val binaryError = reason as BinaryProtocolError
        assertEquals(PREPEND, binaryError.operation)
        assertEquals(Status.UNKNOWN, binaryError.status)
        assertEquals("my-error", binaryError.errorMessage)
    }

    @Test
    fun `prepend value when no reply`() {
        givenOperation(PrependOperation(SOME_KEY, "Hol".toByteArray(), Reply.NO_REPLY))

        expectHeaderSent(
            opCode = PREPENDQ,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        whenRun()

        thenOperationResultIs(AppendPrependResult.NoReply)
    }
}
