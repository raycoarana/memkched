package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.binary.model.OpCode.ADD
import com.raycoarana.memkched.internal.binary.model.OpCode.ADDQ
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import com.raycoarana.memkched.internal.result.AddReplaceResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class AddOperationUnitTest : BaseOperationUnitTest<AddReplaceResult>() {
    @Test
    fun `add value when is stored`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = ADD,
            key = SOME_KEY,
            extras = byteArrayOf(
                // Flags
                0x00, 0x00, 0x00, 0x00,
                // Expiration
                0x00, 0x00, 0x00, 0x64,
            ),
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectSuccessHeaderReceived(opCode = ADD)

        whenRun()

        thenOperationResultIs(AddReplaceResult.Stored)
    }

    @Test
    fun `add value when key exists`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = ADD,
            key = SOME_KEY,
            extras = byteArrayOf(
                // Flags
                0x00, 0x00, 0x00, 0x00,
                // Expiration
                0x00, 0x00, 0x00, 0x64,
            ),
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(ADD, Status.KEY_EXISTS, "error"))

        whenRun()

        thenOperationResultIs(AddReplaceResult.NotStored)
    }

    @Test
    fun `add value when unknown error`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = ADD,
            key = SOME_KEY,
            extras = byteArrayOf(
                // Flags
                0x00, 0x00, 0x00, 0x00,
                // Expiration
                0x00, 0x00, 0x00, 0x64,
            ),
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        expectErrorHeaderReceived(BinaryProtocolError(ADD, Status.UNKNOWN, "my-error"))

        val reason = assertThrows<MemcachedException> {
            whenRun()
        }.reason

        val binaryError = reason as BinaryProtocolError
        assertEquals(ADD, binaryError.operation)
        assertEquals(Status.UNKNOWN, binaryError.status)
        assertEquals("my-error", binaryError.errorMessage)
    }

    @Test
    fun `add value when no reply`() {
        givenOperation(AddOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.NO_REPLY))

        expectHeaderSent(
            opCode = ADDQ,
            key = SOME_KEY,
            extras = byteArrayOf(
                // Flags
                0x00, 0x00, 0x00, 0x00,
                // Expiration
                0x00, 0x00, 0x00, 0x64,
            ),
            body = charArrayOf(
                'H', 'o', 'l'
            ).map { it.code.toByte() }.toByteArray()
        )

        whenRun()

        thenOperationResultIs(AddReplaceResult.NoReply)
    }
}
