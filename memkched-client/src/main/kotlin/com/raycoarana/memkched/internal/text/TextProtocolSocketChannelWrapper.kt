package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.internal.SocketChannelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.math.min

internal class TextProtocolSocketChannelWrapper(
    inBufferSize: Int,
    private val readTimeout: Long
) : SocketChannelWrapper() {
    private val inBuffer = ByteArray(inBufferSize)
    private var position = 0
    private var limit = 0
    private lateinit var socket: Socket
    private lateinit var input: InputStream
    private lateinit var output: OutputStream

    fun wrap(socket: Socket) {
        reset()
        this.socket = socket
        socket.soTimeout = readTimeout.toInt()
        input = socket.getInputStream()
        output = BufferedOutputStream(socket.getOutputStream())
    }

    override fun reset() {
        position = 0
        limit = 0
    }

    suspend fun writeBinary(byteArray: ByteArray) {
        output.write(byteArray)
        output.write(EOL_BYTE_ARRAY)
    }

    suspend fun writeLine(line: String) {
        output.write(line.toByteArray(Charsets.US_ASCII))
        output.write(EOL_BYTE_ARRAY)
    }

    suspend fun writeLineAndBinary(line: String, byteArray: ByteArray) {
        output.write(line.toByteArray(Charsets.US_ASCII))
        output.write(EOL_BYTE_ARRAY)
        output.write(byteArray)
        output.write(EOL_BYTE_ARRAY)
    }

    suspend fun flush() {
        output.flush()
    }

    suspend fun readBinary(size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        while (offset < size) {
            if (position == limit) {
                val read = input.read(result, offset, size - offset)
                if (read < 0) {
                    error("Socket closed while reading binary payload")
                }
                offset += read
            } else {
                val read = min(limit - position, size - offset)
                inBuffer.copyInto(result, offset, position, position + read)
                position += read
                offset += read
            }
        }
        readEOL()
        return result
    }

    suspend fun readLine(): String {
        val lineBuilder = StringBuilder()
        do {
            if (position == limit) {
                refill()
            }
            while (position < limit) {
                val current = inBuffer[position++].toInt().toChar()
                if (lineBuilder.length > 1 && lineBuilder.last() == '\r' && current == '\n') {
                    return lineBuilder.substring(0, lineBuilder.length - 1)
                }
                lineBuilder.append(current)
            }
        } while (true)
    }

    private fun readEOL() {
        val eof1 = readByte().toInt().toChar()
        val eof2 = readByte().toInt().toChar()
        if (eof1 != '\r' || eof2 != '\n') {
            error("Protocol error: EOL not found!")
        }
    }

    private fun readByte(): Byte {
        if (position == limit) {
            refill()
        }
        return inBuffer[position++]
    }

    private fun refill() {
        val read = input.read(inBuffer)
        if (read < 0) {
            error("Socket closed while reading")
        }
        position = 0
        limit = read
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            if (::socket.isInitialized) {
                socket.close()
            }
        }
    }
}
