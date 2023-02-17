package com.raycoarana.memkched.internal

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
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
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult

/**
 * Factory interface to build the different operations
 */
internal interface OperationFactory<T : SocketChannelWrapper> {
    fun get(key: String): Operation<T, GetResult<ByteArray>>
    fun get(keys: List<String>): Operation<T, Map<String, GetResult<ByteArray>>>
    fun gets(key: String): Operation<T, GetsResult<ByteArray>>
    fun gets(keys: List<String>): Operation<T, Map<String, GetsResult<ByteArray>>>
    fun gat(key: String, expiration: Expiration): Operation<T, GatResult<ByteArray>>
    fun gat(keys: List<String>, expiration: Expiration): Operation<T, Map<String, GatResult<ByteArray>>>
    fun gats(key: String, expiration: Expiration): Operation<T, GatsResult<ByteArray>>
    fun gats(keys: List<String>, expiration: Expiration): Operation<T, Map<String, GatsResult<ByteArray>>>
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
    ): Operation<T, AddReplaceResult>
    fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, AddReplaceResult>
    fun append(
        key: String,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, AppendPrependResult>
    fun prepend(
        key: String,
        data: ByteArray,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, AppendPrependResult>
    fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        replay: Reply = Reply.DEFAULT
    ): Operation<T, CasResult>
    fun touch(key: String, expiration: Expiration, replay: Reply = Reply.DEFAULT): Operation<T, TouchResult>
    fun incr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, IncrDecrResult>
    fun decr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): Operation<T, IncrDecrResult>
    fun delete(key: String, replay: Reply = Reply.DEFAULT): Operation<T, DeleteResult>
    fun flushAll(replay: Reply = Reply.DEFAULT): Operation<T, FlushAllResult>
}
