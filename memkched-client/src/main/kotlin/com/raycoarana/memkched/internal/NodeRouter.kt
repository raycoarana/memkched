package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.HashAlgorithm
import com.raycoarana.memkched.api.NodeLocatorType
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.TreeMap
import java.util.zip.CRC32
import java.lang.Math.floorMod

internal interface NodeRouter {
    fun nodeIndex(key: String): Int

    companion object {
        fun create(
            locatorType: NodeLocatorType,
            hashAlgorithm: HashAlgorithm,
            addresses: Array<InetSocketAddress>
        ): NodeRouter = when (locatorType) {
            NodeLocatorType.ARRAY_MOD -> ArrayModNodeRouter(hashAlgorithm, addresses.size)
            NodeLocatorType.CONSISTENT -> ConsistentNodeRouter(hashAlgorithm, addresses)
            NodeLocatorType.VBUCKET -> throw UnsupportedOperationException(
                "VBUCKET node locator is reserved for API compatibility and is not supported yet."
            )
        }
    }
}

internal class ArrayModNodeRouter(
    private val hashAlgorithm: HashAlgorithm,
    private val nodeCount: Int
) : NodeRouter {
    override fun nodeIndex(key: String): Int = floorMod(hashAlgorithm.hash(key), nodeCount.toLong()).toInt()
}

internal class ConsistentNodeRouter(
    private val hashAlgorithm: HashAlgorithm,
    addresses: Array<InetSocketAddress>
) : NodeRouter {
    private val continuum = TreeMap<Long, Int>()

    init {
        addresses.forEachIndexed { index, address ->
            val nodeKey = "${address.hostString}:${address.port}"
            repeat(40) { replica ->
                val digest = md5("$nodeKey-$replica")
                repeat(4) { point ->
                    continuum[ketamaHash(digest, point)] = index
                }
            }
        }
    }

    override fun nodeIndex(key: String): Int {
        val hash = if (hashAlgorithm == HashAlgorithm.KETAMA_HASH) {
            ketamaHash(md5(key), 0)
        } else {
            hashAlgorithm.hash(key) and HASH_MASK
        }
        return continuum.ceilingEntry(hash)?.value ?: continuum.firstEntry().value
    }
}

private const val HASH_MASK = 0xffffffffL

internal fun HashAlgorithm.hash(key: String): Long = when (this) {
    HashAlgorithm.NATIVE_HASH -> key.hashCode().toLong() and HASH_MASK
    HashAlgorithm.CRC_HASH -> CRC32().apply { update(key.toByteArray(Charsets.UTF_8)) }.value
    HashAlgorithm.FNV1_64_HASH -> fnv64(key, false)
    HashAlgorithm.FNV1A_64_HASH -> fnv64(key, true)
    HashAlgorithm.FNV1_32_HASH -> fnv32(key, false).toLong() and HASH_MASK
    HashAlgorithm.FNV1A_32_HASH -> fnv32(key, true).toLong() and HASH_MASK
    HashAlgorithm.KETAMA_HASH -> ketamaHash(md5(key), 0)
}

private fun fnv32(key: String, alternate: Boolean): Int {
    var hash = 0x811c9dc5.toInt()
    key.toByteArray(Charsets.UTF_8).forEach { byte ->
        if (alternate) {
            hash = hash xor byte.toInt()
            hash *= 16777619
        } else {
            hash *= 16777619
            hash = hash xor byte.toInt()
        }
    }
    return hash
}

private fun fnv64(key: String, alternate: Boolean): Long {
    var hash = -3750763034362895579L
    key.toByteArray(Charsets.UTF_8).forEach { byte ->
        if (alternate) {
            hash = hash xor byte.toLong()
            hash *= 1099511628211L
        } else {
            hash *= 1099511628211L
            hash = hash xor byte.toLong()
        }
    }
    return hash and Long.MAX_VALUE
}

private fun md5(value: String): ByteArray =
    MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))

private fun ketamaHash(digest: ByteArray, index: Int): Long {
    val offset = index * 4
    return (digest[offset + 3].toLong() and 0xffL shl 24) or
        (digest[offset + 2].toLong() and 0xffL shl 16) or
        (digest[offset + 1].toLong() and 0xffL shl 8) or
        (digest[offset].toLong() and 0xffL)
}
