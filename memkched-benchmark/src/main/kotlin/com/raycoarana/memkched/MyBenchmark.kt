package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.SetResult
import kotlinx.coroutines.runBlocking
import net.spy.memcached.CachedData
import net.spy.memcached.MemcachedClient
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode.Throughput
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

private const val DATA_SIZE_RESPONSE_PART = 3

@BenchmarkMode(Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
open class MyBenchmark {
    @Benchmark
    fun memkchedSet(state: MemkchedState): SetResult =
        runBlocking {
            state.client.set(KEY, DATA, Transcoder.IDENTITY, Relative(FIVE_MINUTES))
        }

    @Benchmark
    fun memkchedGet(state: MemkchedState): GetGatResult<ByteArray> =
        runBlocking {
            state.client.get(KEY, Transcoder.IDENTITY)
        }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun memkchedSetBatch(state: MemkchedState) {
        runBlocking {
            repeat(BATCH_SIZE) {
                state.client.set(KEY, DATA, Transcoder.IDENTITY, Relative(FIVE_MINUTES))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun memkchedGetBatch(state: MemkchedState) {
        runBlocking {
            repeat(BATCH_SIZE) {
                state.client.get(KEY, Transcoder.IDENTITY)
            }
        }
    }

    @Benchmark
    fun spymemcachedSet(state: SpyMemcachedState): Boolean =
        state.client.set(KEY, FIVE_MINUTES, DATA, state.transcoder).get()

    @Benchmark
    fun spymemcachedGet(state: SpyMemcachedState): ByteArray? =
        state.client.get(KEY, state.transcoder)

    @Benchmark
    fun rawTextSet(state: RawTextState): Boolean =
        state.client.set(KEY, DATA, FIVE_MINUTES)

    @Benchmark
    fun rawTextGet(state: RawTextState): ByteArray? =
        state.client.get(KEY)

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun rawTextSetBatch(state: RawTextState) {
        repeat(BATCH_SIZE) {
            state.client.set(KEY, DATA, FIVE_MINUTES)
        }
    }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun rawTextGetBatch(state: RawTextState) {
        repeat(BATCH_SIZE) {
            state.client.get(KEY)
        }
    }

    @Benchmark
    fun synchronizedRawTextSet(state: SynchronizedRawTextState): Boolean =
        state.client.set(KEY, DATA, FIVE_MINUTES)

    @Benchmark
    fun synchronizedRawTextGet(state: SynchronizedRawTextState): ByteArray? =
        state.client.get(KEY)

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun synchronizedRawTextSetBatch(state: SynchronizedRawTextState) {
        repeat(BATCH_SIZE) {
            state.client.set(KEY, DATA, FIVE_MINUTES)
        }
    }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    fun synchronizedRawTextGetBatch(state: SynchronizedRawTextState) {
        repeat(BATCH_SIZE) {
            state.client.get(KEY)
        }
    }

    @Benchmark
    fun pipelinedRawTextSet(state: PipelinedRawTextState): Boolean =
        state.client.set(KEY, DATA, FIVE_MINUTES)

    @Benchmark
    fun pipelinedRawTextGet(state: PipelinedRawTextState): ByteArray? =
        state.client.get(KEY)

    @State(Scope.Benchmark)
    open class MemkchedState {
        lateinit var client: com.raycoarana.memkched.MemkchedClient

        @Setup
        fun setUp() {
            client = MemkchedClientBuilder()
                .node(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT))
                .build()
            runBlocking {
                client.initialize()
                client.set(KEY, DATA, Transcoder.IDENTITY, Relative(FIVE_MINUTES))
            }
        }

        @TearDown
        fun tearDown() {
            runBlocking {
                client.stop()
            }
        }
    }

    @State(Scope.Benchmark)
    open class SpyMemcachedState {
        lateinit var client: MemcachedClient
        val transcoder: net.spy.memcached.transcoders.Transcoder<ByteArray> = RawByteArrayTranscoder

        @Setup
        fun setUp() {
            client = MemcachedClient(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT))
            client.set(KEY, FIVE_MINUTES, DATA, transcoder).get()
        }

        @TearDown
        fun tearDown() {
            client.shutdown()
        }
    }

    @State(Scope.Benchmark)
    open class RawTextState {
        lateinit var client: RawTextClient

        @Setup
        fun setUp() {
            client = RawTextClient(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT))
            client.connect()
            client.set(KEY, DATA, FIVE_MINUTES)
        }

        @TearDown
        fun tearDown() {
            client.close()
        }
    }

    @State(Scope.Benchmark)
    open class SynchronizedRawTextState {
        lateinit var client: SynchronizedRawTextClient

        @Setup
        fun setUp() {
            client = SynchronizedRawTextClient(RawTextClient(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT)))
            client.connect()
            client.set(KEY, DATA, FIVE_MINUTES)
        }

        @TearDown
        fun tearDown() {
            client.close()
        }
    }

    @State(Scope.Benchmark)
    open class PipelinedRawTextState {
        lateinit var client: PipelinedRawTextClient

        @Setup
        fun setUp() {
            client = PipelinedRawTextClient(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT))
            client.connect()
            client.set(KEY, DATA, FIVE_MINUTES)
        }

        @TearDown
        fun tearDown() {
            client.close()
        }
    }

    companion object {
        private val DATA = """
            |{
            |   "prop1": "value1",
            |   "prop2": "value2",
            |}
        """.trimMargin().toByteArray()
        private const val KEY = "my-key"
        private const val BATCH_SIZE = 100
        private const val FIVE_MINUTES = 300
        private const val MEMCACHED_DEFAULT_PORT = 11211
    }
}

private object RawByteArrayTranscoder : net.spy.memcached.transcoders.Transcoder<ByteArray> {
    override fun asyncDecode(data: CachedData): Boolean = false
    override fun encode(value: ByteArray): CachedData = CachedData(0, value, maxSize)
    override fun decode(data: CachedData): ByteArray = data.data
    override fun getMaxSize(): Int = CachedData.MAX_SIZE
}

class RawTextClient(
    private val address: InetSocketAddress
) {
    private val inBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
    private var position = 0
    private var limit = 0
    private lateinit var socket: Socket
    private lateinit var input: InputStream
    private lateinit var output: OutputStream

    fun connect() {
        socket = Socket()
        socket.tcpNoDelay = true
        socket.connect(address)
        input = socket.getInputStream()
        output = socket.getOutputStream()
    }

    fun set(key: String, data: ByteArray, expirationSeconds: Int): Boolean {
        writeAscii("set $key 0 $expirationSeconds ${data.size}")
        output.write(CRLF)
        output.write(data)
        output.write(CRLF)
        output.flush()
        return readLine() == STORED
    }

    fun get(key: String): ByteArray? {
        writeAscii("get $key")
        output.write(CRLF)
        output.flush()

        val line = readLine()
        if (line == END) {
            return null
        }

        val parts = line.split(' ')
        val data = readBinary(parts[DATA_SIZE_RESPONSE_PART].toInt())
        check(readLine() == END)
        return data
    }

    fun close() {
        if (::socket.isInitialized) {
            socket.close()
        }
    }

    private fun writeAscii(value: String) {
        output.write(value.toByteArray(Charsets.US_ASCII))
    }

    private fun readBinary(size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        while (offset < size) {
            if (position == limit) {
                val read = input.read(result, offset, size - offset)
                check(read >= 0) { "Socket closed while reading binary payload" }
                offset += read
            } else {
                val read = min(limit - position, size - offset)
                inBuffer.copyInto(result, offset, position, position + read)
                position += read
                offset += read
            }
        }
        readCrLf()
        return result
    }

    private fun readLine(): String {
        val lineBuilder = StringBuilder()
        while (true) {
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
        }
    }

    private fun readCrLf() {
        check(readByte().toInt().toChar() == '\r')
        check(readByte().toInt().toChar() == '\n')
    }

    private fun readByte(): Byte {
        if (position == limit) {
            refill()
        }
        return inBuffer[position++]
    }

    private fun refill() {
        val read = input.read(inBuffer)
        check(read >= 0) { "Socket closed while reading" }
        position = 0
        limit = read
    }

    companion object {
        private val CRLF = "\r\n".toByteArray(Charsets.US_ASCII)
        private const val END = "END"
        private const val STORED = "STORED"
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}

class SynchronizedRawTextClient(
    private val delegate: RawTextClient
) {
    private val lock = ReentrantLock()

    fun connect() {
        delegate.connect()
    }

    fun set(key: String, data: ByteArray, expirationSeconds: Int): Boolean =
        lock.withLock {
            delegate.set(key, data, expirationSeconds)
        }

    fun get(key: String): ByteArray? =
        lock.withLock {
            delegate.get(key)
        }

    fun close() {
        delegate.close()
    }
}

class PipelinedRawTextClient(
    private val address: InetSocketAddress
) {
    private val requests = LinkedBlockingQueue<PipelinedRequest>()
    private val batch = ArrayList<PipelinedRequest>(PIPELINE_MAX)
    private val inBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
    private var position = 0
    private var limit = 0
    private lateinit var socket: Socket
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var worker: Thread

    @Volatile
    private var running = false

    fun connect() {
        socket = Socket()
        socket.tcpNoDelay = true
        socket.connect(address)
        input = socket.getInputStream()
        output = socket.getOutputStream()
        running = true
        worker = Thread(::processLoop, "memkched-pipelined-raw-text")
        worker.isDaemon = true
        worker.start()
    }

    fun set(key: String, data: ByteArray, expirationSeconds: Int): Boolean {
        val request = PipelinedRequest.Set(key, data, expirationSeconds)
        requests.put(request)
        return request.future.get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS) as Boolean
    }

    fun get(key: String): ByteArray? {
        val request = PipelinedRequest.Get(key)
        requests.put(request)
        return request.future.get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS) as ByteArray?
    }

    fun close() {
        running = false
        if (::worker.isInitialized) {
            worker.interrupt()
        }
        if (::socket.isInitialized) {
            socket.close()
        }
    }

    private fun processLoop() {
        try {
            while (running) {
                batch.clear()
                batch.add(requests.take())
                requests.drainTo(batch, PIPELINE_MAX - 1)

                for (request in batch) {
                    writeRequest(request)
                }
                output.flush()

                for (request in batch) {
                    readResponse(request)
                }
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (ex: IOException) {
            failPending(ex)
        }
    }

    private fun writeRequest(request: PipelinedRequest) {
        when (request) {
            is PipelinedRequest.Get -> {
                writeAscii("get ${request.key}")
                output.write(CRLF)
            }
            is PipelinedRequest.Set -> {
                writeAscii("set ${request.key} 0 ${request.expirationSeconds} ${request.data.size}")
                output.write(CRLF)
                output.write(request.data)
                output.write(CRLF)
            }
        }
    }

    private fun readResponse(request: PipelinedRequest) {
        when (request) {
            is PipelinedRequest.Get -> request.future.complete(readGetResponse())
            is PipelinedRequest.Set -> request.future.complete(readLine() == STORED)
        }
    }

    private fun readGetResponse(): ByteArray? {
        val line = readLine()
        if (line == END) {
            return null
        }

        val parts = line.split(' ')
        val data = readBinary(parts[DATA_SIZE_RESPONSE_PART].toInt())
        check(readLine() == END)
        return data
    }

    private fun failPending(ex: Throwable) {
        for (request in batch) {
            request.future.completeExceptionally(ex)
        }
        while (true) {
            val request = requests.poll() ?: break
            request.future.completeExceptionally(ex)
        }
    }

    private fun writeAscii(value: String) {
        output.write(value.toByteArray(Charsets.US_ASCII))
    }

    private fun readBinary(size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        while (offset < size) {
            if (position == limit) {
                val read = input.read(result, offset, size - offset)
                if (read < 0) {
                    throw EOFException("Socket closed while reading binary payload")
                }
                offset += read
            } else {
                val read = min(limit - position, size - offset)
                inBuffer.copyInto(result, offset, position, position + read)
                position += read
                offset += read
            }
        }
        readCrLf()
        return result
    }

    private fun readLine(): String {
        val lineBuilder = StringBuilder()
        while (true) {
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
        }
    }

    private fun readCrLf() {
        check(readByte().toInt().toChar() == '\r')
        check(readByte().toInt().toChar() == '\n')
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
            throw EOFException("Socket closed while reading")
        }
        position = 0
        limit = read
    }

    private sealed class PipelinedRequest {
        val future: CompletableFuture<Any?> = CompletableFuture()

        class Get(val key: String) : PipelinedRequest()
        class Set(
            val key: String,
            val data: ByteArray,
            val expirationSeconds: Int
        ) : PipelinedRequest()
    }

    companion object {
        private val CRLF = "\r\n".toByteArray(Charsets.US_ASCII)
        private const val END = "END"
        private const val STORED = "STORED"
        private const val PIPELINE_MAX = 64
        private const val DEFAULT_BUFFER_SIZE = 8192
        private const val OPERATION_TIMEOUT_SECONDS = 5L
    }
}
