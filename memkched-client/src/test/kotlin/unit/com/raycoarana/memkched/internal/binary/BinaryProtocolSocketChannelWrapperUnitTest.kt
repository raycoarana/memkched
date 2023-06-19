package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.internal.binary.model.DATA_TYPE_RAW
import com.raycoarana.memkched.internal.binary.model.MAGIC_REQUEST
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.binary.model.OpCode.ADD
import com.raycoarana.memkched.internal.binary.model.OpCode.ADDQ
import com.raycoarana.memkched.internal.binary.model.Status
import com.raycoarana.memkched.internal.error.MemcachedError.BinaryProtocolError
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.test.assertEquals

class BinaryProtocolSocketChannelWrapperUnitTest {
    private val socketChannel: AsynchronousSocketChannel = mockk()

    private val channelWrapper = BinaryProtocolSocketChannelWrapper(
        4096,
        4096,
        31,
        30
    )

    private val writes = mutableListOf<ByteArray>()

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `read 64 bit long`() = runBlocking {
        channelWrapper.wrap(socketChannel)

        givenReadWillReturn(listOf(ubyteArrayOf(
            0x80u, 0x00u, 0x00u, 0x00u,
            0x00u, 0x00u, 0x00u, 0x01u,
        ).toByteArray()))

        val result: ULong = channelWrapper.readULong()

        assertEquals(9223372036854775809UL, result)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readHeaderProvider")
    fun `read package header successfully`(case: String, receivedBytesPackages: List<ByteArray>, expectedResult: HeaderReadResult) = runBlocking {
        channelWrapper.wrap(socketChannel)

        givenReadWillReturn(receivedBytesPackages)

        val result: HeaderReadResult = channelWrapper.readHeader(
            headerProcess = { opCode, keyLength, extrasLength, totalBodyLength, opaque, cas ->
                SuccessHeader(opCode, keyLength, extrasLength, totalBodyLength, opaque, cas)
            },
            errorProcess = { error, key -> ErrorHeader(error, key) }
        )

        assertEquals(expectedResult, result)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readIncompleteHeaderProvider")
    fun `read incomplete or invalid package header`(case: String, receivedBytesPackages: List<ByteArray>, expectedException: Exception) =
        runBlocking {
            channelWrapper.wrap(socketChannel)

            givenReadWillReturn(receivedBytesPackages)

            val exception = Assertions.assertThrows(expectedException.javaClass) {
                runBlocking {
                    channelWrapper.readHeader(
                        headerProcess = { opCode, keyLength, extrasLength, totalBodyLength, opaque, cas ->
                            SuccessHeader(opCode, keyLength, extrasLength, totalBodyLength, opaque, cas)
                        },
                        errorProcess = { error, key -> ErrorHeader(error, key) }
                    )
                }
            }

            assertEquals(expectedException.message, exception.message)
        }

    private fun givenReadWillReturn(packages: List<ByteArray>) {
        var callCount = 0
        every { socketChannel.read(any(), 31, MILLISECONDS, any<Int>(), any()) } answers {
            val handler: CompletionHandler<Int, Continuation<Int>> = lastArg()
            val continuation = arg<Continuation<Int>>(3)
            if (callCount < packages.size) {
                val buffer: ByteBuffer = firstArg()

                buffer.put(packages[callCount])

                callCount++
                handler.completed(packages.size, continuation)
            } else {
                handler.failed(TimeoutException("Timeout after 31 millis"), continuation)
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writePackageProvider")
    fun `write package`(case: String, writeRequest: WriteRequest, expectedBytesPackages: List<ByteArray>) =
        runBlocking {
            channelWrapper.wrap(socketChannel)

            givenWriteWillSuccess()

            runBlocking {
                channelWrapper.writePackage(
                    writeRequest.opCode,
                    writeRequest.magicNumber,
                    writeRequest.dataType,
                    writeRequest.reserved,
                    writeRequest.opaque,
                    writeRequest.cas,
                    writeRequest.key,
                    writeRequest.extras,
                    writeRequest.body
                )
            }

            assertEquals(expectedBytesPackages.size, writes.size, "Number of writes do not match with expectation.")
            for (i in expectedBytesPackages.indices) {
                assertArrayEquals(expectedBytesPackages[i], writes[i])
            }
        }

    @Test
    fun `write package with all defaults`() =
        runBlocking {
            channelWrapper.wrap(socketChannel)

            givenWriteWillSuccess()

            runBlocking {
                channelWrapper.writePackage(ADDQ)
            }

            assertArrayEquals(
                byteArrayOf(
                    (0x80u).toByte(), 0x12, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                ),
                writes.first()
            )
        }

    private fun givenWriteWillSuccess() {
        every { socketChannel.write(any(), 30, MILLISECONDS, any<Int>(), any()) } answers {
            val handler: CompletionHandler<Int, Continuation<Int>> = lastArg()
            val continuation = arg<Continuation<Int>>(3)
            val buffer: ByteBuffer = firstArg()

            val byteArray = ByteArray(buffer.limit())
            buffer.get(byteArray)
            writes.add(byteArray)

            handler.completed(byteArray.size, continuation)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    companion object {
        @JvmStatic
        fun readHeaderProvider(): List<Arguments> = listOf(
            Arguments.of(
                "Complete no-error header in single package",
                listOf(
                    ubyteArrayOf(
                        0x81u, 0x02u, 0x00u, 0x23u,
                        0x03u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x2Fu,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray()
                ),
                SuccessHeader(ADD, 35, 3, 47, 345, 88888L)
            ),
            Arguments.of(
                "Complete no-error header two packages",
                listOf(
                    ubyteArrayOf(
                        0x81u, 0x02u, 0x00u, 0x23u,
                        0x03u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x2Fu,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                    ).toByteArray(),
                    ubyteArrayOf(
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray()
                ),
                SuccessHeader(ADD, 35, 3, 47, 345, 88888L)
            ),
            Arguments.of(
                "Complete key not found error header single package",
                listOf(
                    ubyteArrayOf(
                        0x81u, 0x02u, 0x00u, 0x08u,
                        0x00u, 0x00u, 0x00u, 0x01u,
                        0x00u, 0x00u, 0x00u, 0x0Du,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray(),
                    charArrayOf(
                        'a', 'b', 'c', 'd',
                        'f', 'g', 'h', 'i',
                    ).map { it.code.toUByte() }.toUByteArray().toByteArray(),
                    charArrayOf(
                        'e', 'r', 'r', 'o',
                        'r',
                    ).map { it.code.toUByte() }.toUByteArray().toByteArray()
                ),
                ErrorHeader(BinaryProtocolError(ADD, Status.KEY_NOT_FOUND, "error"), "abcdfghi")
            ),
            Arguments.of(
                "Complete key not found error header single package without key",
                listOf(
                    ubyteArrayOf(
                        0x81u, 0x02u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x01u,
                        0x00u, 0x00u, 0x00u, 0x05u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray(),
                    charArrayOf(
                        'e', 'r', 'r', 'o',
                        'r',
                    ).map { it.code.toUByte() }.toUByteArray().toByteArray()
                ),
                ErrorHeader(BinaryProtocolError(ADD, Status.KEY_NOT_FOUND, "error"), null)
            ),
            Arguments.of(
                "Complete key not found error header single package without key and message",
                listOf(
                    ubyteArrayOf(
                        0x81u, 0x02u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x01u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray()
                ),
                ErrorHeader(BinaryProtocolError(ADD, Status.KEY_NOT_FOUND, ""), null)
            ),
        )
        @JvmStatic
        fun readIncompleteHeaderProvider(): List<Arguments> = listOf(
            Arguments.of(
                "Complete header in single package with wrong magic number",
                listOf(
                    ubyteArrayOf(
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                    ).toByteArray()
                ),
                IllegalArgumentException("Unexpected response magic number -1")
            ),
            Arguments.of(
                "Complete header in two packages with wrong magic number",
                listOf(
                    ubyteArrayOf(
                        0x08u, 0xFFu, 0xFFu, 0xFFu,
                    ).toByteArray(),
                    ubyteArrayOf(
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                    ).toByteArray()
                ),
                IllegalArgumentException("Unexpected response magic number 8")
            ),
            Arguments.of(
                "Incomplete header",
                listOf(
                    ubyteArrayOf(
                        0xFFu, 0xFFu, 0xFFu, 0xFFu,
                    ).toByteArray()
                ),
                TimeoutException("Timeout after 31 millis")
            ),
        )

        @JvmStatic
        fun writePackageProvider(): List<Arguments> = listOf(
            Arguments.of(
                "Write package without key, extras or body",
                WriteRequest(ADD, MAGIC_REQUEST, 3, 0, 345, 88888L),
                listOf(
                    ubyteArrayOf(
                        0x80u, 0x02u, 0x00u, 0x00u,
                        0x00u, 0x03u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                    ).toByteArray()
                ),
            ),
            Arguments.of(
                "Write package without extras or body",
                WriteRequest(ADD, MAGIC_REQUEST, 3, 0, 345, 88888L, "key"),
                listOf(
                    ubyteArrayOf(
                        0x80u, 0x02u, 0x00u, 0x03u,
                        0x00u, 0x03u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x03u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                        'k'.code.toUByte(), 'e'.code.toUByte(), 'y'.code.toUByte(),
                    ).toByteArray()
                ),
            ),
            Arguments.of(
                "Write package without body",
                WriteRequest(ADD, MAGIC_REQUEST, 3, 0, 345, 88888L, "key", byteArrayOf(
                    0x00, 0x01, 0x02, 0x03
                )),
                listOf(
                    ubyteArrayOf(
                        0x80u, 0x02u, 0x00u, 0x03u,
                        0x04u, 0x03u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x07u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                        0x00u, 0x01u, 0x02u, 0x03u,
                        'k'.code.toUByte(), 'e'.code.toUByte(), 'y'.code.toUByte(),
                    ).toByteArray()
                ),
            ),
            Arguments.of(
                "Write package with all",
                WriteRequest(ADD, MAGIC_REQUEST, 3, 0, 345, 88888L, "key", byteArrayOf(
                    0x00, 0x01, 0x02, 0x03
                ), byteArrayOf(
                    0x33, 0x44
                )),
                listOf(
                    ubyteArrayOf(
                        0x80u, 0x02u, 0x00u, 0x03u,
                        0x04u, 0x03u, 0x00u, 0x00u,
                        0x00u, 0x00u, 0x00u, 0x09u,
                        0x00u, 0x00u, 0x01u, 0x59u,
                        0x00u, 0x00u, 0x00u, 0x00u,
                        0x00u, 0x01u, 0x5Bu, 0x38u,
                        0x00u, 0x01u, 0x02u, 0x03u,
                        'k'.code.toUByte(), 'e'.code.toUByte(), 'y'.code.toUByte(),
                        0x33u, 0x44u
                    ).toByteArray()
                ),
            ),
        )
    }

    sealed interface HeaderReadResult

    data class SuccessHeader(
        val opCode: OpCode,
        val keyLength: Short,
        val extrasLength: Byte,
        val totalBodyLength: Int,
        val opaque: Int,
        val cas: Long
    ) : HeaderReadResult

    data class ErrorHeader(
        val error: BinaryProtocolError,
        val key: String?
    ) : HeaderReadResult

    data class WriteRequest(
        val opCode: OpCode,
        val magicNumber: Byte = MAGIC_REQUEST,
        val dataType: Byte = DATA_TYPE_RAW,
        val reserved: Short = 0,
        val opaque: Int = 0,
        val cas: Long = 0L,
        val key: String? = null,
        val extras: ByteArray? = null,
        val body: ByteArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (other == null || other !is WriteRequest) {
                return false
            }
            return Objects.equals(opCode, other.opCode)
                && Objects.equals(magicNumber, other.magicNumber)
                && Objects.equals(dataType, other.dataType)
                && Objects.equals(reserved, other.reserved)
                && Objects.equals(opaque, other.opaque)
                && Objects.equals(cas, other.cas)
                && Objects.equals(key, other.key)
                && Objects.equals(extras, other.extras)
                && Objects.equals(body, other.body)
        }

        override fun hashCode(): Int {
            var result = opCode.hashCode()
            result = 31 * result + magicNumber
            result = 31 * result + dataType
            result = 31 * result + reserved
            result = 31 * result + opaque
            result = 31 * result + cas.hashCode()
            result = 31 * result + (key?.hashCode() ?: 0)
            result = 31 * result + (extras?.contentHashCode() ?: 0)
            result = 31 * result + (body?.contentHashCode() ?: 0)
            return result
        }
    }
}
