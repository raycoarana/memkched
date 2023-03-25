package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.internal.SocketChannelWrapper
import com.raycoarana.memkched.internal.binary.model.DATA_TYPE_RAW
import com.raycoarana.memkched.internal.binary.model.MAGIC_REQUEST
import com.raycoarana.memkched.internal.binary.model.MAGIC_RESPONSE
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import java.nio.ByteBuffer
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

internal class BinaryProtocolSocketChannelWrapper(
    inBufferSize: Int,
    outBufferSize: Int,
    private val readTimeout: Long,
    private val writeTimeout: Long
) : SocketChannelWrapper() {
    private val inBuffer = ByteBuffer.allocateDirect(inBufferSize)
    private val outBuffer = ByteBuffer.allocateDirect(outBufferSize)

    override fun reset() {
        inBuffer.clear()
        inBuffer.limit(0)
        outBuffer.clear()
    }

    suspend fun <T> readHeader(
        headerProcess: suspend (OpCode, Short, Byte, Int, Int, Long) -> T,
        errorProcess: suspend (BinaryProtocolError, String?) -> T
    ): T {
        read(HEADER_SIZE)
        inBuffer.flip()
        val magicNumber: Byte = inBuffer.get()
        val opCode: OpCode = OpCode.from(inBuffer.get())
        val keyLength: Short = inBuffer.getShort()
        val extrasLength: Byte = inBuffer.get()
        val dataType: Byte = inBuffer.get()
        val status: Status = Status.from(inBuffer.getShort())
        val totalBodyLength: Int = inBuffer.getInt()
        val opaque: Int = inBuffer.getInt()
        val cas: Long = inBuffer.getLong()

        require(magicNumber == MAGIC_RESPONSE) { "Unexpected response magic number $magicNumber" }
        require(dataType == DATA_TYPE_RAW) { "Unexpected data type $dataType" }

        return if (status == Status.NO_ERROR) {
            headerProcess(opCode, keyLength, extrasLength, totalBodyLength, opaque, cas)
        } else {
            require(extrasLength.toInt() == 0) { "Unexpected having extras with error" }
            val keyLengthInt = keyLength.toInt()
            val key = if (keyLengthInt != 0) {
                String(readBinary(keyLengthInt), Charsets.US_ASCII)
            } else {
                null
            }

            val errorMessage = String(readBinary(totalBodyLength - extrasLength - keyLength), Charsets.US_ASCII)

            errorProcess(BinaryProtocolError(opCode, status, errorMessage), key)
        }
    }

    suspend fun readBinary(size: Int): ByteArray {
        val result = ByteArray(size)
        readBinaryChunk(size, result)
        return result
    }

    private suspend inline fun readBinaryChunk(
        size: Int,
        result: ByteArray
    ): Int {
        var offset = 0
        var read = 0
        while (offset < size) {
            if (inBuffer.position() == inBuffer.limit()) {
                read = read(size - offset)
                inBuffer.flip()
            } else {
                read = min(inBuffer.limit() - inBuffer.position(), size - offset)
            }
            inBuffer.get(result, offset, read)
            offset += read
        }
        return read
    }

    private suspend inline fun read(byteToRead: Int? = null) =
        suspendCoroutine { continuation ->
            val limit = min(inBuffer.capacity(), byteToRead ?: Int.MAX_VALUE)
            inBuffer.clear().limit(limit)
            channel.read(inBuffer, readTimeout, MILLISECONDS, continuation, Handler)
        }

    suspend fun writePackage(
        opCode: OpCode,
        magicNumber: Byte = MAGIC_REQUEST,
        dataType: Byte = DATA_TYPE_RAW,
        reserved: Short = 0,
        opaque: Int = 0,
        cas: Long = 0L,
        key: String? = null,
        extras: ByteArray? = null,
        body: ByteArray? = null
    ) {
        val keyBytes = key?.toByteArray(Charsets.US_ASCII)
        val keyLength = (keyBytes?.size ?: 0).toShort()
        val extrasLength = (extras?.size ?: 0).toByte()
        val safeBody = body ?: ByteArray(0)
        val bodyLength = safeBody.size
        val totalBodyLength = keyLength + extrasLength + bodyLength

        outBuffer.clear()
            .put(magicNumber)
            .put(opCode.code)
            .putShort(keyLength)
            .put(extrasLength)
            .put(dataType)
            .putShort(reserved)
            .putInt(totalBodyLength)
            .putInt(opaque)
            .putLong(cas)

        extras?.let { outBuffer.put(it) }
        keyBytes?.let { outBuffer.put(it) }

        var offset = 0
        if (bodyLength == 0) {
            writeChunk()
        } else {
            while (offset < bodyLength) {
                val length = min(outBuffer.capacity(), bodyLength - offset)
                outBuffer.put(safeBody, offset, length)
                offset += writeChunk()
                outBuffer.clear()
            }
        }
    }

    private suspend inline fun writeChunk(): Int =
        suspendCoroutine { continuation ->
            outBuffer.flip()
            channel.write(outBuffer, writeTimeout, MILLISECONDS, continuation, Handler)
        }

    object Handler : CompletionHandler<Int, Continuation<Int>> {
        override fun completed(result: Int, attachment: Continuation<Int>) =
            attachment.resume(result)

        override fun failed(ex: Throwable, attachment: Continuation<Int>) =
            attachment.resumeWithException(ex)
    }

    companion object {
        private const val HEADER_SIZE = 24
    }
}
