package com.raycoarana.memkched.internal.binary.model

enum class Status(val code: Short, val status: String) {
    NO_ERROR(0x0000.toShort(), "No error"),
    KEY_NOT_FOUND(0x0001.toShort(), "Key not found"),
    KEY_EXISTS(0x0002.toShort(), "Key exists"),
    VALUE_TOO_LARGE(0x0003.toShort(), "Value too large"),
    INVALID_ARGUMENTS(0x0004.toShort(), "Invalid arguments"),
    ITEM_NOT_STORED(0x0005.toShort(), "Item not stored"),
    INCR_DECR_ON_NON_NUMERIC_VALUE(0x0006.toShort(), "Incr/Decr on non-numeric value"),
    UNKNOWN_COMMAND(0x0081.toShort(), "Unknown command"),
    OUT_OF_MEMORY(0x0082.toShort(), "Out of memory"),
    UNKNOWN(0xFFFF.toShort(), "Unknown error");

    companion object {
        fun from(code: Short): Status =
            when (code) {
                NO_ERROR.code -> NO_ERROR
                KEY_NOT_FOUND.code -> KEY_NOT_FOUND
                KEY_EXISTS.code -> KEY_EXISTS
                VALUE_TOO_LARGE.code -> VALUE_TOO_LARGE
                INVALID_ARGUMENTS.code -> INVALID_ARGUMENTS
                ITEM_NOT_STORED.code -> ITEM_NOT_STORED
                INCR_DECR_ON_NON_NUMERIC_VALUE.code -> INCR_DECR_ON_NON_NUMERIC_VALUE
                UNKNOWN_COMMAND.code -> UNKNOWN_COMMAND
                OUT_OF_MEMORY.code -> OUT_OF_MEMORY
                else -> UNKNOWN
            }
    }
}
