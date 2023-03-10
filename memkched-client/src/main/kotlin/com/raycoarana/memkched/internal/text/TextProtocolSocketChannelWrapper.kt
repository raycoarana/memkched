package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.SocketChannelWrapper
import java.nio.ByteBuffer
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

internal class TextProtocolSocketChannelWrapper(
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

    suspend fun writeBinary(byteArray: ByteArray): Unit =
        write(byteArray)

    suspend fun writeLine(line: String) {
        val byteArray = line.toByteArray(Charsets.US_ASCII)
        write(byteArray)
    }

    suspend fun readBinary(size: Int): ByteArray {
        val result = ByteArray(size)
        readBinaryChunk(size, result)
        readEOL()
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

    private suspend inline fun readEOL() {
        var read = min(inBuffer.limit() - inBuffer.position(), 2)
        while (read < 2) {
            read += read(2 - read)
        }
        val eof1 = inBuffer.get().toInt().toChar()
        val eof2 = inBuffer.get().toInt().toChar()
        if (eof1 != '\r' || eof2 != '\n') {
            error("Protocol error: EOL not found!")
        }
    }

    suspend fun readLine(): String {
        val lineBuilder = StringBuilder()
        do {
            if (inBuffer.position() == inBuffer.limit()) {
                read()
                inBuffer.flip()
            }
            while (inBuffer.position() < inBuffer.limit()) {
                val current = inBuffer.get().toInt().toChar()
                if (lineBuilder.length > 1 && lineBuilder.last() == '\r' && current == '\n') {
                    return lineBuilder.substring(0, lineBuilder.length - 1)
                }
                lineBuilder.append(current)
            }
        } while (true)
    }

    private suspend inline fun read(byteToRead: Int? = null) =
        suspendCoroutine { continuation ->
            val limit = min(inBuffer.capacity(), byteToRead ?: Int.MAX_VALUE)
            inBuffer.clear().limit(limit)
            channel.read(inBuffer, readTimeout, MILLISECONDS, continuation, Handler)
        }

    private suspend inline fun write(byteArray: ByteArray) {
        var offset = 0
        var eolSent = false
        while (offset < byteArray.size) {
            outBuffer.clear()
            val length = min(outBuffer.capacity(), byteArray.size - offset)
            outBuffer.put(byteArray, offset, length)
            if (length + 2 <= outBuffer.capacity()) {
                // include EOL
                outBuffer.put(EOL_BYTE_ARRAY)
                eolSent = true
            }
            offset += writeChunk()
        }
        if (!eolSent) {
            // smell that buffer is too short
            outBuffer.clear().put(EOL_BYTE_ARRAY)
            writeChunk()
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
}
