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
import java.net.InetSocketAddress

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
