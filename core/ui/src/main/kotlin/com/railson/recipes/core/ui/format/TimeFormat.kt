package com.railson.recipes.core.ui.format

import kotlin.time.Duration

/**
 * The largest unit and the one right below it, zero units skipped (docs/specs/domain.md, Time).
 * 3630 s is "1 h": the seconds are two units below hours, so they are dropped.
 */
fun Duration.formatTime(): String = toComponents { hours, minutes, seconds, _ ->
    listOf(hours to "h", minutes.toLong() to "min", seconds.toLong() to "s")
        .dropWhile { (value, _) -> value == 0L }
        .take(2)
        .filter { (value, _) -> value > 0 }
        .joinToString(" ") { (value, unit) -> "$value $unit" }
        .ifEmpty { "0 s" }
}
