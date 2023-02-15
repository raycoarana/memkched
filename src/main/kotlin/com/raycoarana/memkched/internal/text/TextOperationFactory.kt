package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.Operation
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.result.*

class TextOperationFactory : OperationFactory<TextProtocolSocketChannelWrapper> {
    override fun get(key: String): Operation<TextProtocolSocketChannelWrapper, GetResult> {
        TODO("Not yet implemented")
    }

    override fun get(keys: List<String>): Operation<TextProtocolSocketChannelWrapper, MultiGetResult> {
        TODO("Not yet implemented")
    }

    override fun gets(key: String): Operation<TextProtocolSocketChannelWrapper, GetsResult> {
        TODO("Not yet implemented")
    }

    override fun gets(keys: List<String>): Operation<TextProtocolSocketChannelWrapper, MultiGetsResult> {
        TODO("Not yet implemented")
    }

    override fun gat(key: String, expiration: Expiration): Operation<TextProtocolSocketChannelWrapper, GatResult> {
        TODO("Not yet implemented")
    }

    override fun gat(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, MultiGatResult> {
        TODO("Not yet implemented")
    }

    override fun gats(key: String, expiration: Expiration): Operation<TextProtocolSocketChannelWrapper, GatsResult> {
        TODO("Not yet implemented")
    }

    override fun gats(
        keys: List<String>,
        expiration: Expiration
    ): Operation<TextProtocolSocketChannelWrapper, MultiGatsResult> {
        TODO("Not yet implemented")
    }

    override fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, SetResult> {
        TODO("Not yet implemented")
    }

    override fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, AddResult> {
        TODO("Not yet implemented")
    }

    override fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, ReplaceResult> {
        TODO("Not yet implemented")
    }

    override fun append(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, AppendResult> {
        TODO("Not yet implemented")
    }

    override fun prepend(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, PrependResult> {
        TODO("Not yet implemented")
    }

    override fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        dataSize: Int,
        casUnique: CasUnique,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, CasResult> {
        TODO("Not yet implemented")
    }

    override fun touch(
        key: String,
        expiration: Expiration,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, TouchResult> {
        TODO("Not yet implemented")
    }

    override fun incr(
        key: String,
        value: ULong,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, IncrResult> {
        TODO("Not yet implemented")
    }

    override fun decr(
        key: String,
        value: ULong,
        replay: Reply
    ): Operation<TextProtocolSocketChannelWrapper, DecrResult> {
        TODO("Not yet implemented")
    }

    override fun delete(key: String, replay: Reply): Operation<TextProtocolSocketChannelWrapper, DeleteResult> {
        TODO("Not yet implemented")
    }

    override fun flushAll(replay: Reply): Operation<TextProtocolSocketChannelWrapper, FlushAllResult> {
        TODO("Not yet implemented")
    }
}
