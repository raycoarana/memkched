package com.raycoarana.memkched.internal.binary.operation

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import com.raycoarana.memkched.internal.binary.model.OpCode
import com.raycoarana.memkched.internal.result.GetGatResult
import com.raycoarana.memkched.internal.result.GetGatResult.NotFound
import com.raycoarana.memkched.internal.result.GetGatResult.Value

internal open class GetOperation(
    key: String
) : AbstractGetOperation<GetGatResult<ByteArray>>(OpCode.GET, key) {
    override fun mapToValue(flags: Flags, value: ByteArray, cas: CasUnique) = Value(flags, value)
    override fun mapToNotFound() = NotFound
}
