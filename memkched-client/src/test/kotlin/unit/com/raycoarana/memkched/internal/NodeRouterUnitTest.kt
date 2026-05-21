package com.raycoarana.memkched.internal

import com.raycoarana.memkched.MemkchedClientBuilder
import com.raycoarana.memkched.api.HashAlgorithm
import com.raycoarana.memkched.api.NodeLocatorType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetSocketAddress
import java.util.TreeMap
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeRouterUnitTest {
    private val addresses = arrayOf(
        InetSocketAddress("localhost", 11211),
        InetSocketAddress("localhost", 11212),
        InetSocketAddress("localhost", 11213)
    )

    @Test
    fun `array mod locator selects stable nodes`() {
        val router = NodeRouter.create(NodeLocatorType.ARRAY_MOD, HashAlgorithm.NATIVE_HASH, addresses)

        val first = router.nodeIndex("some-key")
        val second = router.nodeIndex("some-key")

        assertEquals(first, second)
        assertTrue(first in addresses.indices)
    }

    @Test
    fun `ketama locator selects deterministic nodes`() {
        val router = NodeRouter.create(NodeLocatorType.CONSISTENT, HashAlgorithm.KETAMA_HASH, addresses)

        val selectedNodes = listOf("alpha", "beta", "gamma", "delta").map { router.nodeIndex(it) }

        assertEquals(selectedNodes, listOf("alpha", "beta", "gamma", "delta").map { router.nodeIndex(it) })
        selectedNodes.forEach { assertTrue(it in addresses.indices) }
    }

    @Test
    fun `array mod locator keeps unsigned hash range when selecting nodes`() {
        val router = NodeRouter.create(NodeLocatorType.ARRAY_MOD, HashAlgorithm.NATIVE_HASH, addresses)
        val key = generateSequence(0) { it + 1 }
            .map { "key-$it" }
            .first { HashAlgorithm.NATIVE_HASH.hash(it) > Int.MAX_VALUE }

        assertEquals((HashAlgorithm.NATIVE_HASH.hash(key) % addresses.size).toInt(), router.nodeIndex(key))
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `consistent locator normalizes sixty four bit hashes to the ring range`() {
        val router = NodeRouter.create(NodeLocatorType.CONSISTENT, HashAlgorithm.FNV1_64_HASH, addresses) as ConsistentNodeRouter
        val continuum = ConsistentNodeRouter::class.java.getDeclaredField("continuum")
            .apply { isAccessible = true }
            .get(router) as TreeMap<Long, Int>
        val firstNode = continuum.firstEntry().value
        val key = generateSequence(0) { it + 1 }
            .map { "key-$it" }
            .first {
                val rawHash = HashAlgorithm.FNV1_64_HASH.hash(it)
                val normalizedHash = rawHash and 0xffffffffL
                val expectedNode = continuum.ceilingEntry(normalizedHash)?.value ?: firstNode
                rawHash > continuum.lastKey() && expectedNode != firstNode
            }
        val normalizedHash = HashAlgorithm.FNV1_64_HASH.hash(key) and 0xffffffffL
        val expectedNode = continuum.ceilingEntry(normalizedHash)?.value ?: firstNode

        assertEquals(expectedNode, router.nodeIndex(key))
    }

    @Test
    fun `vbucket locator is rejected by builder`() {
        val ex = assertThrows<UnsupportedOperationException> {
            MemkchedClientBuilder()
                .node(addresses.first())
                .locatorType(NodeLocatorType.VBUCKET)
                .build()
        }

        assertTrue(ex.message?.contains("VBUCKET") == true)
    }
}
