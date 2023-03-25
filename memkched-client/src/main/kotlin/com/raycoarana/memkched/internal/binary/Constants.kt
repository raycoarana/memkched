package com.raycoarana.memkched.internal.binary

internal const val MAGIC_REQUEST = 0x80.toByte()
internal const val MAGIC_RESPONSE = 0x81.toByte()

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
            when(code) {
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

enum class OpCode(val code: Byte, val opName: String) {
    GET(0x00.toByte(), "Get"),
    SET(0x01.toByte(), "Set"),
    ADD(0x02.toByte(), "Add"),
    REPLACE(0x03.toByte(), "Replace"),
    DELETE(0x04.toByte(), "Delete"),
    INCREMENT(0x05.toByte(), "Increment"),
    DECREMENT(0x06.toByte(), "Decrement"),
    QUIT(0x07.toByte(), "Quit"),
    FLUSH(0x08.toByte(), "Flush"),
    GETQ(0x09.toByte(), "GetQ"),
    NOOP(0x0A.toByte(), "No-op"),
    VERSION(0x0B.toByte(), "Version"),
    GETK(0x0C.toByte(), "GetK"),
    GETKQ(0x0D.toByte(), "GetKQ"),
    APPEND(0x0E.toByte(), "Append"),
    PREPEND(0x0F.toByte(), "Prepend"),
    STAT(0x10.toByte(), "Stat"),
    SETQ(0x11.toByte(), "SetQ"),
    ADDQ(0x12.toByte(), "AddQ"),
    REPLACEQ(0x13.toByte(), "ReplaceQ"),
    DELETEQ(0x14.toByte(), "DeleteQ"),
    INCREMENTQ(0x15.toByte(), "IncrementQ"),
    DECREMENTQ(0x16.toByte(), "DecrementQ"),
    QUITQ(0x17.toByte(), "QuitQ"),
    FLUSHQ(0x18.toByte(), "FlushQ"),
    APPENDQ(0x19.toByte(), "AppendQ"),
    PREPENDQ(0x1A.toByte(), "PrependQ"),
    UNKNOWN(0xFF.toByte(), "Unknown");

    companion object {
        fun from(code: Byte): OpCode =
            when(code) {
                GET.code -> GET
                SET.code -> SET
                ADD.code -> ADD
                REPLACE.code -> REPLACE
                DELETE.code -> DELETE
                INCREMENT.code -> INCREMENT
                DECREMENT.code -> DECREMENT
                QUIT.code -> QUIT
                FLUSH.code -> FLUSH
                GETQ.code -> GETQ
                NOOP.code -> NOOP
                VERSION.code -> VERSION
                GETK.code -> GETK
                GETKQ.code -> GETKQ
                APPEND.code -> APPEND
                PREPEND.code -> PREPEND
                STAT.code -> STAT
                SETQ.code -> SETQ
                ADDQ.code -> ADDQ
                REPLACEQ.code -> REPLACEQ
                DELETEQ.code -> DELETEQ
                INCREMENTQ.code -> INCREMENTQ
                DECREMENTQ.code -> DECREMENTQ
                QUITQ.code -> QUITQ
                FLUSHQ.code -> FLUSHQ
                APPENDQ.code -> APPENDQ
                PREPENDQ.code -> PREPENDQ
                else -> UNKNOWN
            }
    }
}

internal const val DATA_TYPE_RAW = 0x00.toByte()
