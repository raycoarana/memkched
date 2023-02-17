package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.FlushAllResult
import com.raycoarana.memkched.internal.result.GatResult
import com.raycoarana.memkched.internal.result.GatsResult
import com.raycoarana.memkched.internal.result.GetResult
import com.raycoarana.memkched.internal.result.GetsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.MultiGatResult
import com.raycoarana.memkched.internal.result.MultiGatsResult
import com.raycoarana.memkched.internal.result.MultiGetResult
import com.raycoarana.memkched.internal.result.MultiGetsResult
import com.raycoarana.memkched.internal.result.Result
import com.raycoarana.memkched.internal.result.TouchResult
import com.raycoarana.memkched.internal.text.operation.SetOperation

internal class TextOperationFactory : OperationFactory<TextProtocolSocketChannelWrapper> {
    override fun get(key: String): Operation<TextProtocolSocketChannelWrapper, Result<GetResult>> {
        TODO("Not yet implemented")
    }

    override fun get(keys: List<String>): Operation<TextProtocolSocketChannelWrapper, Result<MultiGetResult>> {
        TODO("Not yet implemented")
    }

    override fun gets(key: String): Operation<TextProtocolSocketChannelWrapper, Result<GetsResult>> {
        TODO("Not yet implemented")
    }

    override fun gets(keys: List<String>): Operation<TextProtocolSocketChannelWrapper, Result<MultiGetsResult>> {
        TODO("Not yet implemented")
    }

    override fun gat(
        key: String,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Result<GatResult>> {
        TODO("Not yet implemented")
    }

    override fun gat(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Result<MultiGatResult>> {
        TODO("Not yet implemented")
    }

    override fun gats(
        key: String,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Result<GatsResult>> {
        TODO("Not yet implemented")
    }

    override fun gats(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, Result<MultiGatsResult>> {
        TODO("Not yet implemented")
    }

    override fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply
    ) = SetOperation(key, flags, expiration, data, replay)

    override fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<AddReplaceResult>> {
        TODO("Not yet implemented")
    }

    override fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<AddReplaceResult>> {
        TODO("Not yet implemented")
    }

    override fun append(
        key: String,
        data: ByteArray,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<AppendPrependResult>> {
        TODO("Not yet implemented")
    }

    override fun prepend(
        key: String,
        data: ByteArray,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<AppendPrependResult>> {
        TODO("Not yet implemented")
    }

    override fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<CasResult>> {
        TODO("Not yet implemented")
    }

    override fun touch(
        key: String,
        expiration: Expiration,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<TouchResult>> {
        TODO("Not yet implemented")
    }

    override fun incr(
        key: String,
        value: ULong,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<IncrDecrResult>> {
        TODO("Not yet implemented")
    }

    override fun decr(
        key: String,
        value: ULong,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, Result<IncrDecrResult>> {
        TODO("Not yet implemented")
    }

    override fun delete(key: String, replay: Reply): Operation<TextProtocolSocketChannelWrapper, Result<DeleteResult>> {
        TODO("Not yet implemented")
    }

    override fun flushAll(replay: Reply): Operation<TextProtocolSocketChannelWrapper, Result<FlushAllResult>> {
        TODO("Not yet implemented")
    }
}
