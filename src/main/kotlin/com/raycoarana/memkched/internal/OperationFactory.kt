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
    fun get(key: String): Operation<T, GetResult>
    fun get(keys: List<String>): Operation<T, MultiGetResult>
    fun gets(key: String): Operation<T, GetsResult>
    fun gets(keys: List<String>): Operation<T, MultiGetsResult>
    fun gat(key: String, expiration: Expiration): Operation<T, GatResult>
    fun gat(keys: List<String>, expiration: Expiration): Operation<T, MultiGatResult>
    fun gats(key: String, expiration: Expiration): Operation<T, GatsResult>
    fun gats(keys: List<String>, expiration: Expiration): Operation<T, MultiGatsResult>
    fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, SetResult>
    fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, AddResult>
    fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, ReplaceResult>
    fun append(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, AppendResult>
    fun prepend(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, PrependResult>
    fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, CasResult>
    fun touch(key: String, expiration: Expiration, replay: Reply = Reply.DEFAULT): Operation<T, TouchResult>
    fun incr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, IncrResult>
    fun decr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, DecrResult>
    fun delete(key: String, replay: Reply = Reply.DEFAULT): Operation<T, DeleteResult>
    fun flushAll(replay: Reply = Reply.DEFAULT): Operation<T, FlushAllResult>
}
