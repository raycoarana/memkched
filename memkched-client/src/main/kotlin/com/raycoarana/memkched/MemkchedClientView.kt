package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.result.AddReplaceResult
import com.raycoarana.memkched.internal.result.AppendPrependResult
import com.raycoarana.memkched.internal.result.CasResult
import com.raycoarana.memkched.internal.result.DeleteResult
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.IncrDecrResult
import com.raycoarana.memkched.internal.result.SetResult
import com.raycoarana.memkched.internal.result.TouchResult

class MemkchedClientView<T> internal constructor(
    private val memkchedClient: MemkchedClient,
    private val transcoder: Transcoder<T>
) {
    /**
     * Memcached GET operation of a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @return GetGatResult child class with the Value or NotFound
     */
    suspend fun get(key: String): GetGatResult<T> =
        memkchedClient.get(key, transcoder)

    /**
     * Memcached GET operation of a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @return a Map of GetGatResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun get(keys: List<String>): Map<String, GetGatResult<T>> =
        memkchedClient.get(keys, transcoder)

    /**
     * Memcached GETS operation of a single key returning its cas unique
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @return GetsGatsResult child class with the Value or NotFound
     */
    suspend fun gets(key: String): GetsGatsResult<T> =
        memkchedClient.gets(key, transcoder)

    /**
     * Memcached GETS operation of a set keys returning its cas unique
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @return a Map of GetsGatsResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun gets(keys: List<String>): Map<String, GetsGatsResult<T>> =
        memkchedClient.gets(keys, transcoder)

    /**
     * Memcached GAT operation that allows to get and touch a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time to set to item
     * @return GetGatResult child class with the Value or NotFound
     */
    suspend fun gat(key: String, expiration: Expiration): GetGatResult<T> =
        memkchedClient.gat(key, expiration, transcoder)

    /**
     * Memcached GAT operation that allows to get and touch a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param expiration expiration time to set to all items
     * @return a Map of GetGatResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun gat(keys: List<String>, expiration: Expiration): Map<String, GetGatResult<T>> =
        memkchedClient.gat(keys, expiration, transcoder)

    /**
     * Memcached GATS operation that allows to get and touch a single key returning its cas unique
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time to set to item
     * @return GetsGatsResult child class with the Value or NotFound
     */
    suspend fun gats(key: String, expiration: Expiration): GetsGatsResult<T> =
        memkchedClient.gats(key, expiration, transcoder)

    /**
     * Memcached GATS operation that allows to get and touch a set keys returning its cas unique
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param expiration expiration time to set to all items
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to gat heterogeneous data.
     * @return a Map of GetsGatsResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun gats(keys: List<String>, expiration: Expiration): Map<String, GetsGatsResult<T>> =
        memkchedClient.gats(keys, expiration, transcoder)

    /***
     * Memcached SET operation to store a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a SetResult child class with the result of the operation as Stored or NoReply in case NoReply were
     * requested
     */
    suspend fun set(
        key: String,
        value: T,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): SetResult =
        memkchedClient.set(key, value, transcoder, expiration, flags, reply)

    /***
     * Memcached ADD operation to store a value only if it not exists already
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AddReplaceResult child class with the result of the operation as Stored/NotStored or NoReply in case
     * NoReply were requested
     */
    suspend fun add(
        key: String,
        value: T,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): AddReplaceResult =
        memkchedClient.add(key, value, transcoder, expiration, flags, reply)

    /***
     * Memcached REPLACE operation to store a value only if it exists already
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AddReplaceResult child class with the result of the operation as Stored/NotStored or NoReply in case
     * NoReply were requested
     */
    suspend fun replace(
        key: String,
        value: T,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): AddReplaceResult =
        memkchedClient.replace(key, value, transcoder, expiration, flags, reply)

    /***
     * Memcached APPEND operation to append data to existing data
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AppendPrependResult child class with the result of the operation as Stored or NoReply in case NoReply
     * were requested
     */
    suspend fun append(
        key: String,
        value: T,
        reply: Reply = Reply.DEFAULT
    ): AppendPrependResult =
        memkchedClient.append(key, value, transcoder, reply)

    /***
     * Memcached PREPEND operation to prepend data to existing data
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AppendPrependResult child class with the result of the operation as Stored or NoReply in case NoReply
     * were requested
     */
    suspend fun prepend(
        key: String,
        value: T,
        reply: Reply = Reply.DEFAULT
    ): AppendPrependResult =
        memkchedClient.prepend(key, value, transcoder, reply)

    /***
     * Memcached CAS operation to store a value only if it has not been modified
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param expiration expiration time of the item
     * @param casUnique unique 64-bits value of the existing item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a CasResult child class with the result of the operation as Stored or NoReply in case NoReply were
     * requested
     */
    suspend fun cas(
        key: String,
        value: T,
        expiration: Expiration,
        casUnique: CasUnique,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): CasResult =
        memkchedClient.cas(key, value, transcoder, expiration, casUnique, flags, reply)

    /***
     * Memcached TOUCH operation to update expiration of existing value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time of the item
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a TouchResult child class with the result of the operation as Touched, NotFound or NoReply in case
     * NoReply were requested
     */
    suspend fun touch(key: String, expiration: Expiration, reply: Reply = Reply.DEFAULT): TouchResult =
        memkchedClient.touch(key, expiration, reply)

    /***
     * Memcached INCR operation to increment a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value amount to increment
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a IncrDecrResult child class with the result of the operation as Value with the new value, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun incr(key: String, value: ULong = 1L.toULong(), reply: Reply = Reply.DEFAULT): IncrDecrResult =
        memkchedClient.incr(key, value, reply)

    /***
     * Memcached DECR operation to increment a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value amount to decrement
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a IncrDecrResult child class with the result of the operation as Value with the new value, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun decr(key: String, value: ULong = 1L.toULong(), reply: Reply = Reply.DEFAULT): IncrDecrResult =
        memkchedClient.decr(key, value, reply)

    /***
     * Memcached DELETE operation to delete a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a DeleteResult child class with the result of the operation as Deleted, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun delete(key: String, reply: Reply = Reply.DEFAULT): DeleteResult =
        memkchedClient.delete(key, reply)
}
