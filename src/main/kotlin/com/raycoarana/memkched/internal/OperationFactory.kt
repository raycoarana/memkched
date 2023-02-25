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
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult

/**
 * Factory interface to build the different operations
 */
internal interface OperationFactory<T : SocketChannelWrapper> {
    fun get(key: String): Operation<T, GetGatResult<ByteArray>>
    fun get(keys: List<String>): Operation<T, Map<String, GetGatResult<ByteArray>>>
    fun gets(key: String): Operation<T, GetsGatsResult<ByteArray>>
    fun gets(keys: List<String>): Operation<T, Map<String, GetsGatsResult<ByteArray>>>
    fun gat(key: String, expiration: Expiration): Operation<T, GetGatResult<ByteArray>>
    fun gat(keys: List<String>, expiration: Expiration): Operation<T, Map<String, GetGatResult<ByteArray>>>
    fun gats(key: String, expiration: Expiration): Operation<T, GetsGatsResult<ByteArray>>
    fun gats(keys: List<String>, expiration: Expiration): Operation<T, Map<String, GetsGatsResult<ByteArray>>>
    fun set(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, SetResult>
    fun add(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, AddReplaceResult>
    fun replace(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, AddReplaceResult>
    fun append(
        key: String,
        data: ByteArray,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, AppendPrependResult>
    fun prepend(
        key: String,
        data: ByteArray,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, AppendPrependResult>
    fun cas(
        key: String,
        flags: Flags,
        expiration: Expiration,
        data: ByteArray,
        casUnique: CasUnique,
        reply: Reply = Reply.DEFAULT
    ): Operation<T, CasResult>
    fun touch(key: String, expiration: Expiration, reply: Reply = Reply.DEFAULT): Operation<T, TouchResult>
    fun incr(key: String, value: ULong, reply: Reply = Reply.DEFAULT): Operation<T, IncrDecrResult>
    fun decr(key: String, value: ULong, reply: Reply = Reply.DEFAULT): Operation<T, IncrDecrResult>
    fun delete(key: String, reply: Reply = Reply.DEFAULT): Operation<T, DeleteResult>
    fun flushAll(expiration: Expiration?, reply: Reply = Reply.DEFAULT): Operation<T, FlushAllResult>
}
