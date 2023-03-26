package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.binary.model.OpCode.GET
import com.raycoarana.memkched.internal.result.GetsGatsResult
import com.raycoarana.memkched.internal.result.GetsGatsResult.NotFound
import com.raycoarana.memkched.internal.result.GetsGatsResult.Value

internal open class GetsOperation(
    key: String
) : AbstractGetOperation<GetsGatsResult<ByteArray>>(GET, key) {
    override fun mapToValue(flags: Flags, value: ByteArray, cas: CasUnique) = Value(flags, value, cas)
    override fun mapToNotFound() = NotFound
}
