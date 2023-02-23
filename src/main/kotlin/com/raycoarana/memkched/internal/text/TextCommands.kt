package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply

/***
 * gat <exptime> <key>*\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 */
internal fun gat(key: String, expiration: Expiration): String =
    "gat ${expiration.value} $key$EOL"

/***
 * gat <key>*\r\n
 *
 * @param keys a maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 */
internal fun gat(keys: List<String>, expiration: Expiration): String =
    keys.joinToString(separator = " ", prefix = "gat ${expiration.value} ", postfix = "$EOL")

/***
 * gats <key>*\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 */
internal fun gats(key: String, expiration: Expiration): String =
    "gats ${expiration.value} $key$EOL"

/***
 * gats <key>*\r\n
 *
 * @param keys a list of keys, each maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 */
internal fun gats(keys: List<String>, expiration: Expiration): String =
    keys.joinToString(separator = " ", prefix = "gats ${expiration.value} ", postfix = "$EOL")

/***
 * touch <key> <exptime> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun touch(key: String, expiration: Expiration, reply: Reply = Reply.DEFAULT): String =
    "touch $key ${expiration.value}${reply.asTextCommandValue()}$EOL"

/***
 * incr <key> <value> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param value 64-bit unsigned integer to increment
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun incr(key: String, value: ULong, reply: Reply = Reply.DEFAULT): String =
    "incr $key $value${reply.asTextCommandValue()}$EOL"

/***
 * decr <key> <value> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param value 64-bit unsigned integer to increment
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun decr(key: String, value: ULong, reply: Reply = Reply.DEFAULT): String =
    "decr $key $value${reply.asTextCommandValue()}$EOL"

/***
 * delete <key> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun delete(key: String, reply: Reply = Reply.DEFAULT): String =
    "delete $key${reply.asTextCommandValue()}$EOL"

/***
 * flush_all <key> [noreply]\r\n
 *
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun flushAll(reply: Reply = Reply.DEFAULT): String =
    "flush_all${reply.asTextCommandValue()}$EOL"
