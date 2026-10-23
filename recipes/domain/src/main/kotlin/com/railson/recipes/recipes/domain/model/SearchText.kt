package com.railson.recipes.recipes.domain.model

import java.text.Normalizer

private val marks = "\\p{M}+".toRegex()

/** Trimmed, lower-cased and accent-stripped, so "  Pão " and "pao" read the same (docs/specs/domain.md, Search). */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD).replace(marks, "").lowercase()
