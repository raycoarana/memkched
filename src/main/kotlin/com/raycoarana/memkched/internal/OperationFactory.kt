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
import com.raycoarana.memkched.internal.result.MultiGatResult
import com.raycoarana.memkched.internal.result.MultiGatsResult
import com.raycoarana.memkched.internal.result.MultiGetResult
import com.raycoarana.memkched.internal.result.MultiGetsResult
import com.raycoarana.memkched.internal.result.Result
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult

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
