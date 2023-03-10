package com.raycoarana.memkched

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Expiration.Relative
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply
import com.raycoarana.memkched.api.Transcoder
import com.raycoarana.memkched.internal.Cluster
import com.raycoarana.memkched.internal.OperationConfig
import com.raycoarana.memkched.internal.OperationFactory
import com.raycoarana.memkched.internal.SocketChannelWrapper
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
import kotlinx.coroutines.channels.Channel

class MemkchedClient internal constructor(
    private val createOperationFactory: OperationFactory<out SocketChannelWrapper>,
    private val cluster: Cluster<out SocketChannelWrapper>,
    private val operationConfig: OperationConfig
) {
    @Suppress("UNCHECKED_CAST")
    private val channel: Channel<Any> = cluster.channel as Channel<Any>

    suspend fun initialize() {
        cluster.start()
    }

    suspend fun stop() {
        cluster.stop()
    }

    fun <T> viewWith(transcoder: Transcoder<T>): MemkchedClientView<T> =
        MemkchedClientView(this, transcoder)

    /**
     * Memcached GET operation of a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetGatResult child class with the Value or NotFound
     */
    suspend fun <T> get(key: String, transcoder: Transcoder<T>): GetGatResult<T> {
        val operation = createOperationFactory.get(key)
        channel.send(operation)

        val getResult = operation.await(operationConfig.timeout)
        return getResult.map { flags, data -> transcoder.decode(flags, data) }
    }

    /**
     * Memcached GET operation of a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to get heterogeneous data.
     * @return a Map of GetGatResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun <T> get(keys: List<String>, transcoder: Transcoder<T>): Map<String, GetGatResult<T>> {
        val operation = createOperationFactory.get(keys)
        channel.send(operation)

        val getResultMap = operation.await(operationConfig.timeout)
        return getResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    /**
     * Memcached GETS operation of a single key returning its cas unique
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetsGatsResult child class with the Value or NotFound
     */
    suspend fun <T> gets(key: String, transcoder: Transcoder<T>): GetsGatsResult<T> {
        val operation = createOperationFactory.gets(key)
        channel.send(operation)

        val getsResult = operation.await(operationConfig.timeout)
        return getsResult.map { flags, data -> transcoder.decode(flags, data) }
    }

    /**
     * Memcached GETS operation of a set keys returning its cas unique
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to get heterogeneous data.
     * @return a Map of GetsGatsResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun <T> gets(keys: List<String>, transcoder: Transcoder<T>): Map<String, GetsGatsResult<T>> {
        val operation = createOperationFactory.gets(keys)
        channel.send(operation)

        val getsResultMap = operation.await(operationConfig.timeout)
        return getsResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    /**
     * Memcached GAT operation that allows to get and touch a single key
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time to set to item
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetGatResult child class with the Value or NotFound
     */
    suspend fun <T> gat(key: String, expiration: Expiration, transcoder: Transcoder<T>): GetGatResult<T> {
        val operation = createOperationFactory.gat(key, expiration)
        channel.send(operation)

        val gatResult = operation.await(operationConfig.timeout)
        return gatResult.map { flags, data -> transcoder.decode(flags, data) }
    }

    /**
     * Memcached GAT operation that allows to get and touch a set keys
     *
     * @param keys a list of keys, each having a maximum of 250 characters, must not include control characters or
     * whitespaces
     * @param expiration expiration time to set to all items
     * @param transcoder converter to apply to byte array data obtained from each key. Transcoder.IDENTITY will return
     * the raw ByteArray, allowing to gat heterogeneous data.
     * @return a Map of GetGatResult child classes with the Value or NotFound, indexed by its key
     */
    suspend fun <T> gat(
        keys: List<String>,
        expiration: Expiration,
        transcoder: Transcoder<T>
    ): Map<String, GetGatResult<T>> {
        val operation = createOperationFactory.gat(keys, expiration)
        channel.send(operation)

        val gatResultMap = operation.await(operationConfig.timeout)
        return gatResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    /**
     * Memcached GATS operation that allows to get and touch a single key returning its cas unique
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time to set to item
     * @param transcoder converter to apply to byte array data obtained from the key. Transcoder.IDENTITY will return
     * the raw ByteArray
     * @return GetsGatsResult child class with the Value or NotFound
     */
    suspend fun <T> gats(key: String, expiration: Expiration, transcoder: Transcoder<T>): GetsGatsResult<T> {
        val operation = createOperationFactory.gats(key, expiration)
        channel.send(operation)

        val gatsResult = operation.await(operationConfig.timeout)
        return gatsResult.map { flags, data -> transcoder.decode(flags, data) }
    }

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
    suspend fun <T> gats(
        keys: List<String>,
        expiration: Expiration,
        transcoder: Transcoder<T>
    ): Map<String, GetsGatsResult<T>> {
        val operation = createOperationFactory.gats(keys, expiration)
        channel.send(operation)

        val gatsResultMap = operation.await(operationConfig.timeout)
        return gatsResultMap.mapValues { it.value.map { flags, data -> transcoder.decode(flags, data) } }
    }

    /***
     * Memcached SET operation to store a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a SetResult child class with the result of the operation as Stored or NoReply in case NoReply were
     * requested
     */
    suspend fun <T> set(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): SetResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.set(key, flags, expiration, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached ADD operation to store a value only if it not exists already
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AddReplaceResult child class with the result of the operation as Stored/NotStored or NoReply in case
     * NoReply were requested
     */
    suspend fun <T> add(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): AddReplaceResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.add(key, flags, expiration, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached REPLACE operation to store a value only if it exists already
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param expiration expiration time of the item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AddReplaceResult child class with the result of the operation as Stored/NotStored or NoReply in case
     * NoReply were requested
     */
    suspend fun <T> replace(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        expiration: Expiration,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): AddReplaceResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.replace(key, flags, expiration, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached APPEND operation to append data to existing data
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AppendPrependResult child class with the result of the operation as Stored or NoReply in case NoReply
     * were requested
     */
    suspend fun <T> append(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        reply: Reply = Reply.DEFAULT
    ): AppendPrependResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.append(key, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached PREPEND operation to prepend data to existing data
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a AppendPrependResult child class with the result of the operation as Stored or NoReply in case NoReply
     * were requested
     */
    suspend fun <T> prepend(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        reply: Reply = Reply.DEFAULT
    ): AppendPrependResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.prepend(key, data, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached CAS operation to store a value only if it has not been modified
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value value to store
     * @param transcoder transcoder to use to convert the value into an array of bytes
     * @param expiration expiration time of the item
     * @param casUnique unique 64-bits value of the existing item
     * @param flags 16-bits flags stored with the value
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a CasResult child class with the result of the operation as Stored or NoReply in case NoReply were
     * requested
     */
    suspend fun <T> cas(
        key: String,
        value: T,
        transcoder: Transcoder<T>,
        expiration: Expiration,
        casUnique: CasUnique,
        flags: Flags = Flags(),
        reply: Reply = Reply.DEFAULT
    ): CasResult {
        val data = transcoder.encode(value)
        val operation = createOperationFactory.cas(key, flags, expiration, data, casUnique, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached TOUCH operation to update expiration of existing value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param expiration expiration time of the item
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a TouchResult child class with the result of the operation as Touched, NotFound or NoReply in case
     * NoReply were requested
     */
    suspend fun touch(key: String, expiration: Expiration, reply: Reply = Reply.DEFAULT): TouchResult {
        val operation = createOperationFactory.touch(key, expiration, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached INCR operation to increment a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value amount to increment
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a IncrDecrResult child class with the result of the operation as Value with the new value, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun incr(key: String, value: ULong = 1L.toULong(), reply: Reply = Reply.DEFAULT): IncrDecrResult {
        val operation = createOperationFactory.incr(key, value, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached DECR operation to increment a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param value amount to decrement
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a IncrDecrResult child class with the result of the operation as Value with the new value, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun decr(key: String, value: ULong = 1L.toULong(), reply: Reply = Reply.DEFAULT): IncrDecrResult {
        val operation = createOperationFactory.decr(key, value, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached DELETE operation to delete a value
     *
     * @param key a maximum of 250 characters key, must not include control characters or whitespaces
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a DeleteResult child class with the result of the operation as Deleted, NotFound or
     * NoReply in case NoReply were requested
     */
    suspend fun delete(key: String, reply: Reply = Reply.DEFAULT): DeleteResult {
        val operation = createOperationFactory.delete(key, reply)
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }

    /***
     * Memcached FLUSH_ALL operation to delete the entire memcached server
     *
     * @param after optional expiration to wait before delete all
     * @param reply optional parameter to instruct the server to not send an answer
     * @return a FlushAllResult child class with the result of the operation as Ok or NoReply in case NoReply were
     * requested
     */
    suspend fun flushAll(after: Relative? = null, reply: Reply = Reply.DEFAULT): FlushAllResult {
        val operation = createOperationFactory.flushAll(after, reply)

        // FIXME #7 flushAll must indicate what worker node to flush
        channel.send(operation)

        return operation.await(operationConfig.timeout)
    }
}
