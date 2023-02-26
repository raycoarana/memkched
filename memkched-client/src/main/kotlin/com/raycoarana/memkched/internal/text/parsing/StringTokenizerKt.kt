package com.raycoarana.memkched.internal.text.parsing

import com.raycoarana.memkched.api.CasUnique
import com.raycoarana.memkched.api.Flags
import java.util.StringTokenizer

fun StringTokenizer.nextIntToken(): Int =
    nextToken().toInt()

fun StringTokenizer.nextFlags(): Flags =
    Flags.from(nextToken().toUShort())

fun StringTokenizer.nextCasUnique(): CasUnique =
    CasUnique(nextToken().toLong())
