package com.raycoarana.memkched.internal.error

import com.raycoarana.memkched.internal.MemcachedException
import com.raycoarana.memkched.internal.text.CLIENT_ERROR
import com.raycoarana.memkched.internal.text.ERROR
import com.raycoarana.memkched.internal.text.SERVER_ERROR

sealed class MemcachedError {
    fun asException() = MemcachedException(this)

    object ProtocolError : MemcachedError() {
        override fun toString() = "ERROR response received"
    }
    data class ClientError(val reason: String) : MemcachedError() {
        override fun toString() = "$CLIENT_ERROR $reason"
    }
    data class ServerError(val reason: String) : MemcachedError() {
        override fun toString() = "$SERVER_ERROR $reason"
    }

    companion object {
        fun parse(response: String): MemcachedError =
            when {
                response == ERROR -> ProtocolError
                response.startsWith(CLIENT_ERROR) -> ClientError(
                    response.substring(CLIENT_ERROR.length + 1)
                )
                response.startsWith(SERVER_ERROR) -> ServerError(
                    response.substring(SERVER_ERROR.length + 1)
                )
                else -> error("Unexpected response received: '$response'")
            }
    }
}
