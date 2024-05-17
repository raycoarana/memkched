package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.binary.model.OpCode.REPLACE
import com.raycoarana.memkched.internal.binary.model.OpCode.REPLACEQ
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import com.raycoarana.memkched.internal.result.AddReplaceResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class ReplaceOperationUnitTest : BaseOperationUnitTest<AddReplaceResult>() {
    @Test
    fun `replace value when is stored`() {
        givenOperation(ReplaceOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = REPLACE,
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

        expectSuccessHeaderReceived(opCode = REPLACE)

        whenRun()

        thenOperationResultIs(AddReplaceResult.Stored)
    }

    @Test
    fun `replace value when key not found`() {
        givenOperation(ReplaceOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = REPLACE,
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

        expectErrorHeaderReceived(BinaryProtocolError(REPLACE, Status.KEY_NOT_FOUND, "error"))

        whenRun()

        thenOperationResultIs(AddReplaceResult.NotStored)
    }

    @Test
    fun `replace value when unknown error`() {
        givenOperation(ReplaceOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.DEFAULT))

        expectHeaderSent(
            opCode = REPLACE,
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

        expectErrorHeaderReceived(BinaryProtocolError(REPLACE, Status.UNKNOWN, "my-error"))

        val reason = assertThrows<MemcachedException> {
            whenRun()
        }.reason

        val binaryError = reason as BinaryProtocolError
        assertEquals(REPLACE, binaryError.operation)
        assertEquals(Status.UNKNOWN, binaryError.status)
        assertEquals("my-error", binaryError.errorMessage)
    }

    @Test
    fun `replace value when no reply`() {
        givenOperation(ReplaceOperation(SOME_KEY, Flags(), SOME_EXPIRATION, "Hol".toByteArray(), Reply.NO_REPLY))

        expectHeaderSent(
            opCode = REPLACEQ,
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
