package com.raycoarana.memkched.internal.result

interface Result {
    interface Error : Result
    interface ClientError : Result {
        val reason: String
    }
    interface ServerError : Result {
        val reason: String
    }
}
