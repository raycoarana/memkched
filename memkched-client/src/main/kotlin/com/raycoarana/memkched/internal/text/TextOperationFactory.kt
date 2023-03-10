package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.text.operation.AddOperation
import com.raycoarana.memkched.internal.text.operation.AppendOperation
import com.raycoarana.memkched.internal.text.operation.CasOperation
import com.raycoarana.memkched.internal.text.operation.DecrOperation
import com.raycoarana.memkched.internal.text.operation.DeleteOperation
import com.raycoarana.memkched.internal.text.operation.FlushAllOperation
import com.raycoarana.memkched.internal.text.operation.GatOperation
import com.raycoarana.memkched.internal.text.operation.GatsOperation
import com.raycoarana.memkched.internal.text.operation.GetOperation
import com.raycoarana.memkched.internal.text.operation.GetsOperation
import com.raycoarana.memkched.internal.text.operation.IncrOperation
import com.raycoarana.memkched.internal.text.operation.MultiGatOperation
import com.raycoarana.memkched.internal.text.operation.MultiGatsOperation
import com.raycoarana.memkched.internal.text.operation.MultiGetOperation
import com.raycoarana.memkched.internal.text.operation.MultiGetsOperation
import com.raycoarana.memkched.internal.text.operation.PrependOperation
import com.raycoarana.memkched.internal.text.operation.ReplaceOperation
import com.raycoarana.memkched.internal.text.operation.SetOperation
import com.raycoarana.memkched.internal.text.operation.TouchOperation

internal class TextOperationFactory : OperationFactory<TextProtocolSocketChannelWrapper> {
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
