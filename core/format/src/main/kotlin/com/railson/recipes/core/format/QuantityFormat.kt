package com.railson.recipes.core.format

import java.util.Locale
import kotlin.math.roundToLong

/** "½", "1½", "1.3", "2", then the unit after a space: "2 pinches", "1 cup" (DESIGN.md 4, Data → UI rules). */
fun formatQuantity(quantity: Double, unit: String?): String {
    val number = quantity.fractionOrNull() ?: quantity.oneDecimal()
    val plural = quantity > 1 && number != "1"
    val shownUnit = if (plural) Plurals[unit] ?: unit else unit
    return listOfNotNull(number, shownUnit).joinToString(" ")
}

@Suppress("MagicNumber") // Hundredths: a quarter, a half, three quarters.
private val Fractions = mapOf(25L to "¼", 50L to "½", 75L to "¾")

// The other units are abbreviations, which have no plural.
private val Plurals = mapOf("pinch" to "pinches", "cup" to "cups")

private const val HUNDREDTHS = 100
private const val TENTHS = 10

/** "¼", "1½" and so on, or null when the quantity isn't a whole number plus a quarter, a half or three quarters. */
private fun Double.fractionOrNull(): String? {
    // In hundredths, so a scaled 0.7500000001 still reads as ¾.
    val hundredths = (this * HUNDREDTHS).roundToLong()
    val whole = hundredths / HUNDREDTHS
    val fraction = Fractions[hundredths % HUNDREDTHS] ?: return null
    return if (whole == 0L) fraction else "$whole$fraction"
}

private fun Double.oneDecimal(): String {
    val tenths = (this * TENTHS).roundToLong()
    return if (tenths % TENTHS == 0L) "${tenths / TENTHS}" else String.format(Locale.ENGLISH, "%.1f", this)
}
