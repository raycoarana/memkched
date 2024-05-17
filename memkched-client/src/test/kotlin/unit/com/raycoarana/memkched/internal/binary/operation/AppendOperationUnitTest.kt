package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.binary.model.OpCode.APPEND
import com.raycoarana.memkched.internal.binary.model.OpCode.APPENDQ
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import com.raycoarana.memkched.internal.result.AppendPrependResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class AppendOperationUnitTest : BaseOperationUnitTest<AppendPrependResult>() {
    @Test
    fun `append value when is stored`() {
        givenOperation(AppendOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = APPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectSuccessHeaderReceived(opCode = APPEND)

        whenRun()

        thenOperationResultIs(AppendPrependResult.Stored)
    }

    @Test
    fun `append value when item not stored`() {
        givenOperation(AppendOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = APPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(APPEND, Status.ITEM_NOT_STORED, "error"))

        whenRun()

        thenOperationResultIs(AppendPrependResult.NotStored)
    }

    @Test
    fun `append value when unknown error`() {
        givenOperation(AppendOperation(SOME_KEY, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = APPEND,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(APPEND, Status.UNKNOWN, "my-error"))

        val reason = assertThrows<MemcachedException> {
            whenRun()
        }.reason

        val binaryError = reason as BinaryProtocolError
        assertEquals(APPEND, binaryError.operation)
        assertEquals(Status.UNKNOWN, binaryError.status)
        assertEquals("my-error", binaryError.errorMessage)
    }

    @Test
    fun `append value when no reply`() {
        givenOperation(AppendOperation(SOME_KEY, "Hol".toByteArray(), Reply.NO_REPLY))

        expectHeaderSent(
            opCode = APPENDQ,
            key = SOME_KEY,
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        whenRun()

        thenOperationResultIs(AppendPrependResult.NoReply)
    }
}
