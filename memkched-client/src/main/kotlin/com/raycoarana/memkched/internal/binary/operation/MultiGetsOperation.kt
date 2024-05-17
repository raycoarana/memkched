package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.NotFound
import com.raycoarana.memkched.internal.result.GetsGatsResult.Value

internal open class MultiGetsOperation(
    keys: List<String>
) : AbstractMultiGetOperation<GetsGatsResult<ByteArray>>(keys) {
    override fun mapToValue(flags: Flags, value: ByteArray, casUnique: CasUnique) = Value(flags, value, casUnique)
    override fun mapToNotFound() = NotFound
}
