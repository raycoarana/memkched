package com.raycoarana.memkched.internal

import com.raycoarana.memkched.internal.error.MemcachedError

class MemcachedException(val reason: MemcachedError) : RuntimeException(reason.toString())
