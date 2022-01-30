package com.sarahisweird.uwubot

import kotlin.time.Duration

fun Long.leftPadTwo() =
    if (this < 10) "0$this" else "$this"

fun Duration.toTimeString() =
    (if (inWholeHours > 0) "${inWholeHours.leftPadTwo()}:" else "") +
        "${(inWholeMinutes - inWholeHours * 60).leftPadTwo()}:${(inWholeSeconds - inWholeMinutes * 60).leftPadTwo()}"

fun Char.repeat(n: Int): String =
    (1..n).map { this }.joinToString("")