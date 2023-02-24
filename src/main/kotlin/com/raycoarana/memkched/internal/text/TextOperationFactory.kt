package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.FlushAllResult
import com.raycoarana.memkched.internal.result.GatResult
import com.raycoarana.memkched.internal.result.GatsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.text.operation.AddOperation
import com.raycoarana.memkched.internal.text.operation.AppendOperation
import com.raycoarana.memkched.internal.text.operation.CasOperation
import com.raycoarana.memkched.internal.text.operation.GetOperation
import com.raycoarana.memkched.internal.text.operation.GetsOperation
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

    override fun gat(
        key: String,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, GatResult<ByteArray>> {
        TODO("Not yet implemented")
    }

    override fun gat(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Map<String, GatResult<ByteArray>>> {
        TODO("Not yet implemented")
    }

    override fun gats(
        key: String,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, GatsResult<ByteArray>> {
        TODO("Not yet implemented")
    }

    override fun gats(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Map<String, GatsResult<ByteArray>>> {
        TODO("Not yet implemented")
    }

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

    override fun incr(
        key: String,
        value: ULong,
        reply: Reply
    ): Operation<TextProtocolSocketChannelWrapper, IncrDecrResult> {
        TODO("Not yet implemented")
    }

    override fun decr(
        key: String,
        value: ULong,
        reply: Reply
    ): Operation<TextProtocolSocketChannelWrapper, IncrDecrResult> {
        TODO("Not yet implemented")
    }

    override fun delete(key: String, reply: Reply): Operation<TextProtocolSocketChannelWrapper, DeleteResult> {
        TODO("Not yet implemented")
    }

    override fun flushAll(reply: Reply): Operation<TextProtocolSocketChannelWrapper, FlushAllResult> {
        TODO("Not yet implemented")
    }
}
