package com.raycoarana.memkched.internal.binary

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.binary.operation.AddOperation
import com.raycoarana.memkched.internal.binary.operation.AppendOperation
import com.raycoarana.memkched.internal.binary.operation.CasOperation
import com.raycoarana.memkched.internal.binary.operation.DecrOperation
import com.raycoarana.memkched.internal.binary.operation.DeleteOperation
import com.raycoarana.memkched.internal.binary.operation.FlushAllOperation
import com.raycoarana.memkched.internal.binary.operation.GatOperation
import com.raycoarana.memkched.internal.binary.operation.GatsOperation
import com.raycoarana.memkched.internal.binary.operation.GetOperation
import com.raycoarana.memkched.internal.binary.operation.GetsOperation
import com.raycoarana.memkched.internal.binary.operation.IncrOperation
import com.raycoarana.memkched.internal.binary.operation.MultiGatOperation
import com.raycoarana.memkched.internal.binary.operation.MultiGatsOperation
import com.raycoarana.memkched.internal.binary.operation.MultiGetOperation
import com.raycoarana.memkched.internal.binary.operation.MultiGetsOperation
import com.raycoarana.memkched.internal.binary.operation.PrependOperation
import com.raycoarana.memkched.internal.binary.operation.ReplaceOperation
import com.raycoarana.memkched.internal.binary.operation.SetOperation
import com.raycoarana.memkched.internal.binary.operation.TouchOperation

internal class BinaryOperationFactory : OperationFactory<BinaryProtocolSocketChannelWrapper> {
    override fun get(key: String) = GetOperation(key)
    override fun get(keys: List<String>) = MultiGetOperation(keys)
    override fun gets(key: String) = GetsOperation(key)
    override fun gets(keys: List<String>) = MultiGetsOperation(keys)
    override fun gat(key: String, expiration: Expiration) = GatOperation(key, expiration)
    override fun gat(keys: List<String>, expiration: Expiration) = MultiGatOperation(keys, expiration)
    override fun gats(key: String, expiration: Expiration) = GatsOperation(key, expiration)
    override fun gats(keys: List<String>, expiration: Expiration) = MultiGatsOperation(keys, expiration)

    override fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply
    ) = SetOperation(key, flags, expiration, data, reply)

    override fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply
    ) = AddOperation(key, flags, expiration, data, reply)

    override fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply
    ) = ReplaceOperation(key, flags, expiration, data, reply)

    override fun append(key: String, data: ByteArray, reply: Reply) = AppendOperation(key, data, reply)
    override fun prepend(key: String, data: ByteArray, reply: Reply) = PrependOperation(key, data, reply)

    override fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        reply: Reply
    ) = CasOperation(key, flags, expiration, data, casUnique, reply)

    override fun touch(key: String, expiration: Expiration, reply: Reply) = TouchOperation(key, expiration, reply)
    override fun incr(key: String, value: ULong, reply: Reply) = IncrOperation(key, value, reply)
    override fun decr(key: String, value: ULong, reply: Reply) = DecrOperation(key, value, reply)
    override fun delete(key: String, reply: Reply) = DeleteOperation(key, reply)
    override fun flushAll(expiration: Expiration?, reply: Reply) = FlushAllOperation(expiration, reply)
}
