package com.raycoarana.memkched.internal.text

import com.raycoarana.memkched.api.Expiration
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
 * flush_all <key> [noreply]\r\n
 *
 * @param reply optional parameter to instruct the server to not send an answer
 */
internal fun flushAll(reply: Reply = Reply.DEFAULT): String =
    "flush_all${reply.asTextCommandValue()}$EOL"
