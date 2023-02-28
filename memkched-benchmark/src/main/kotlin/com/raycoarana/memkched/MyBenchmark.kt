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
        memkchedClient = MemkchedClientBuilder().node(InetSocketAddress("localhost", 11211))
            .build()
    }

    @Group
    @Benchmark
    fun runSet(): SetResult =
        runBlocking {
            memkchedClient.set("my-key", DATA, Transcoder.IDENTITY, Relative(300))
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
    }
}
