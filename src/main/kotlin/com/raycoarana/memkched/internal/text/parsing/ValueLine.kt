package com.raycoarana.memkched.internal.text.parsing

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.text.VALUE
import java.util.*

internal data class ValueLine(
    val key: String,
    val flags: Flags,
    val bytesCount: Int,
    val casUnique: CasUnique?
) {
    companion object {
        fun parseValue(result: String): ValueLine {
            val tokenizer = StringTokenizer(result)
            require(tokenizer.nextToken() == VALUE) {
                "Unexpected response without VALUE: $result"
            }

            val key = tokenizer.nextToken()
            val flags = tokenizer.nextFlags()
            val bytesCount = tokenizer.nextIntToken()
            val casUnique = tokenizer.takeIf { tokenizer.hasMoreTokens() }?.nextCasUnique()

            return ValueLine(key, flags, bytesCount, casUnique)
        }
    }
}
