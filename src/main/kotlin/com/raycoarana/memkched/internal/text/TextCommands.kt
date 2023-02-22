package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Expiration
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.api.Reply

/***
 * gets <key>*\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 */
internal fun gets(key: String): String =
    "gets $key$EOL"

/***
 * gets <key>*\r\n
 *
 * @param keys a list of keys, each maximum of 250 characters key, must not include control characters or whitespaces
 */
internal fun gets(keys: List<String>): String =
    keys.joinToString(separator = " ", prefix = "gets ", postfix = "$EOL")

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
 * set <key> <flags> <exptime> <bytes> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun set(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    replay: Reply = Reply.DEFAULT
): String =
    "set $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL"

/***
 * add <key> <flags> <exptime> <bytes> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun add(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    replay: Reply = Reply.DEFAULT
): String =
    "add $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL"

/***
 * replace <key> <flags> <exptime> <bytes> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun replace(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    replay: Reply = Reply.DEFAULT
): String =
    "replace $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL"

/***
 * append <key> <flags> <exptime> <bytes> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun append(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    replay: Reply = Reply.DEFAULT
): String =
    "append $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL"

/***
 * prepend <key> <flags> <exptime> <bytes> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun prepend(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    replay: Reply = Reply.DEFAULT
): String =
    "prepend $key ${flags.toUShort()} ${expiration.value} $dataSize${replay.asTextCommandValue()}$EOL"

/***
 * cas <key> <flags> <exptime> <bytes> <cas unique> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param flags 16-bits flags
 * @param expiration expiration time of the item
 * @param dataSize number of bytes of data to set
 * @param casUnique unique 64-bits value of the existing item
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun cas(
    key: String,
    flags: Flags,
    expiration: Expiration,
    dataSize: Int,
    casUnique: CasUnique,
    replay: Reply = Reply.DEFAULT
): String =
    "cas $key ${flags.toUShort()} ${expiration.value} $dataSize ${casUnique.value}${replay.asTextCommandValue()}$EOL"

/***
 * touch <key> <exptime> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param expiration new expiration time of the item
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun touch(key: String, expiration: Expiration, replay: Reply = Reply.DEFAULT): String =
    "touch $key ${expiration.value}${replay.asTextCommandValue()}$EOL"

/***
 * incr <key> <value> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param value 64-bit unsigned integer to increment
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun incr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): String =
    "incr $key $value${replay.asTextCommandValue()}$EOL"

/***
 * decr <key> <value> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param value 64-bit unsigned integer to increment
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun decr(key: String, value: ULong, replay: Reply = Reply.DEFAULT): String =
    "decr $key $value${replay.asTextCommandValue()}$EOL"

/***
 * delete <key> [noreply]\r\n
 *
 * @param key a maximum of 250 characters key, must not include control characters or whitespaces
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun delete(key: String, replay: Reply = Reply.DEFAULT): String =
    "delete $key${replay.asTextCommandValue()}$EOL"

/***
 * flush_all <key> [noreply]\r\n
 *
 * @param replay optional parameter to instruct the server to not send an answer
 */
internal fun flushAll(replay: Reply = Reply.DEFAULT): String =
    "flush_all${replay.asTextCommandValue()}$EOL"
