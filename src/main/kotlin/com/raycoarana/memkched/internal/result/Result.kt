package com.raycoarana.memkched.internal.result

import com.raycoarana.memkched.internal.text.CLIENT_ERROR
import com.raycoarana.memkched.internal.text.ERROR
import com.raycoarana.memkched.internal.text.SERVER_ERROR
import org.slf4j.LoggerFactory

sealed class Result<Data> {
    object ProtocolError : Result<Any>()
    data class ClientError<T>(val reason: String) : Result<T>()
    data class ServerError<T>(val reason: String) : Result<T>()

    data class SuccessResult<Data>(val data: Data): Result<Data>()

    companion object {
        private val logger = LoggerFactory.getLogger(Result::class.java)

        @Suppress("UNCHECKED_CAST")
        fun <T> error(result: String): Result<T> =
            when {
                result == ERROR -> ProtocolError as Result<T>
                result.startsWith(CLIENT_ERROR) -> ClientError(result.substring(CLIENT_ERROR.length + 1))
                result.startsWith(SERVER_ERROR) -> ServerError(result.substring(SERVER_ERROR.length + 1))
                else -> {
                    logger.error("Unexpected response received: $result")
                    ProtocolError as Result<T>
                }
            }
    }
}
