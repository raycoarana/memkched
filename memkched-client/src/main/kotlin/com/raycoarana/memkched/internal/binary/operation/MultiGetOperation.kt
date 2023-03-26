package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import com.raycoarana.memkched.internal.result.GetGatResult.Value

internal open class MultiGetOperation(
    keys: List<String>
) : AbstractMultiGetOperation<GetGatResult<ByteArray>>(keys) {
    override fun mapToValue(flags: Flags, value: ByteArray, casUnique: CasUnique) = Value(flags, value)
    override fun mapToNotFound() = NotFound
}
