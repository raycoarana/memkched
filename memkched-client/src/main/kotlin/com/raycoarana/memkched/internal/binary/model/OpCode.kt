package com.raycoarana.memkched.internal.binary.model

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
        @Suppress("CyclomaticComplexMethod")
        fun from(code: Byte): OpCode =
            when (code) {
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