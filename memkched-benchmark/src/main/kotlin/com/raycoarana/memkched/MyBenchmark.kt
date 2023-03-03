package com.raycoarana.memkched

import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.SetResult
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Group
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode.Throughput
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import java.net.InetSocketAddress

@BenchmarkMode(Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Group)
open class MyBenchmark {
    private lateinit var memkchedClient: MemkchedClient

    @Setup
    fun setUp() {
        memkchedClient = MemkchedClientBuilder()
            .node(InetSocketAddress("localhost", MEMCACHED_DEFAULT_PORT))
            .build()
        runBlocking {
            memkchedClient.initialize()
        }
    }

    @TearDown
    fun tearDown() {
        runBlocking {
            memkchedClient.stop()
        }
    }

    @Group
    @Benchmark
    fun runSet(): SetResult =
        runBlocking {
            memkchedClient.set("my-key", DATA, Transcoder.IDENTITY, Relative(FIVE_MINUTES))
        }

    @Group
    @Benchmark
    fun runGet(): GetGatResult<ByteArray> =
        runBlocking {
            memkchedClient.get("my-key", Transcoder.IDENTITY)
        }

    companion object {
        private val DATA = """
            |{
            |   "prop1": "value1",
            |   "prop2": "value2",
            |}
        """.trimMargin().toByteArray()
        private const val FIVE_MINUTES = 300
        private const val MEMCACHED_DEFAULT_PORT = 11211
    }
}
