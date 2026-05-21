package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.HashAlgorithm
import com.raycoarana.memkched.api.NodeLocatorType
import java.lang.Math.floorMod
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.TreeMap
import java.util.zip.CRC32

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
            repeat(KETAMA_REPLICA_COUNT) { replica ->
                val digest = md5("$nodeKey-$replica")
                repeat(KETAMA_POINTS_PER_DIGEST) { point ->
                    continuum[ketamaHash(digest, point)] = index
                }
            }
        }
    }

    override fun nodeIndex(key: String): Int {
        val hash = if (hashAlgorithm == HashAlgorithm.KETAMA_HASH) {
            ketamaHash(md5(key), KETAMA_DEFAULT_POINT_INDEX)
        } else {
            hashAlgorithm.hash(key) and HASH_MASK
        }
        return continuum.ceilingEntry(hash)?.value ?: continuum.firstEntry().value
    }
}

private const val HASH_MASK = 0xffffffffL
private const val BYTE_MASK = 0xffL
private const val KETAMA_REPLICA_COUNT = 40
private const val KETAMA_POINTS_PER_DIGEST = 4
private const val KETAMA_DEFAULT_POINT_INDEX = 0
private const val KETAMA_BYTES_PER_POINT = 4
private const val KETAMA_BYTE_0 = 0
private const val KETAMA_BYTE_1 = 1
private const val KETAMA_BYTE_2 = 2
private const val KETAMA_BYTE_3 = 3
private const val KETAMA_BYTE_1_SHIFT = 8
private const val KETAMA_BYTE_2_SHIFT = 16
private const val KETAMA_BYTE_3_SHIFT = 24
private const val FNV_32_OFFSET_BASIS = 0x811c9dc5.toInt()
private const val FNV_32_PRIME = 16777619
private const val FNV_64_OFFSET_BASIS = -3750763034362895579L
private const val FNV_64_PRIME = 1099511628211L

internal fun HashAlgorithm.hash(key: String): Long = when (this) {
    HashAlgorithm.NATIVE_HASH -> key.hashCode().toLong() and HASH_MASK
    HashAlgorithm.CRC_HASH -> CRC32().apply { update(key.toByteArray(Charsets.UTF_8)) }.value
    HashAlgorithm.FNV1_64_HASH -> fnv64(key, false)
    HashAlgorithm.FNV1A_64_HASH -> fnv64(key, true)
    HashAlgorithm.FNV1_32_HASH -> fnv32(key, false).toLong() and HASH_MASK
    HashAlgorithm.FNV1A_32_HASH -> fnv32(key, true).toLong() and HASH_MASK
    HashAlgorithm.KETAMA_HASH -> ketamaHash(md5(key), KETAMA_DEFAULT_POINT_INDEX)
}

private fun fnv32(key: String, alternate: Boolean): Int {
    var hash = FNV_32_OFFSET_BASIS
    key.toByteArray(Charsets.UTF_8).forEach { byte ->
        if (alternate) {
            hash = hash xor byte.toInt()
            hash *= FNV_32_PRIME
        } else {
            hash *= FNV_32_PRIME
            hash = hash xor byte.toInt()
        }
    }
    return hash
}

private fun fnv64(key: String, alternate: Boolean): Long {
    var hash = FNV_64_OFFSET_BASIS
    key.toByteArray(Charsets.UTF_8).forEach { byte ->
        if (alternate) {
            hash = hash xor byte.toLong()
            hash *= FNV_64_PRIME
        } else {
            hash *= FNV_64_PRIME
            hash = hash xor byte.toLong()
        }
    }
    return hash and Long.MAX_VALUE
}

private fun md5(value: String): ByteArray =
    MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))

private fun ketamaHash(digest: ByteArray, index: Int): Long {
    val offset = index * KETAMA_BYTES_PER_POINT
    return (digest[offset + KETAMA_BYTE_3].toLong() and BYTE_MASK shl KETAMA_BYTE_3_SHIFT) or
        (digest[offset + KETAMA_BYTE_2].toLong() and BYTE_MASK shl KETAMA_BYTE_2_SHIFT) or
        (digest[offset + KETAMA_BYTE_1].toLong() and BYTE_MASK shl KETAMA_BYTE_1_SHIFT) or
        (digest[offset + KETAMA_BYTE_0].toLong() and BYTE_MASK)
}
