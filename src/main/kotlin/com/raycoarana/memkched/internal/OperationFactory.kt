package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.internal.result.*

/**
 * Factory interface to build the different operations
 */
internal interface OperationFactory<T : SocketChannelWrapper> {
    fun get(key: String): Operation<T, Result<GetResult>>
    fun get(keys: List<String>): Operation<T, Result<MultiGetResult>>
    fun gets(key: String): Operation<T, Result<GetsResult>>
    fun gets(keys: List<String>): Operation<T, Result<MultiGetsResult>>
    fun gat(key: String, expiration: Expiration): Operation<T, Result<GatResult>>
    fun gat(keys: List<String>, expiration: Expiration): Operation<T, Result<MultiGatResult>>
    fun gats(key: String, expiration: Expiration): Operation<T, Result<GatsResult>>
    fun gats(keys: List<String>, expiration: Expiration): Operation<T, Result<MultiGatsResult>>
    fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<SetResult>>
    fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<AddReplaceResult>>
    fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<AddReplaceResult>>
    fun append(
        key: String,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<AppendPrependResult>>
    fun prepend(
        key: String,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<AppendPrependResult>>
    fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, Result<CasResult>>
    fun touch(key: String, expiration: Expiration, replay: Reply = Reply.DEFAULT): Operation<T, Result<TouchResult>>
    fun incr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, Result<IncrDecrResult>>
    fun decr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, Result<IncrDecrResult>>
    fun delete(key: String, replay: Reply = Reply.DEFAULT): Operation<T, Result<DeleteResult>>
    fun flushAll(replay: Reply = Reply.DEFAULT): Operation<T, Result<FlushAllResult>>
}
