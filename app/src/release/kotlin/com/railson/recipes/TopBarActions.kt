package com.railson.recipes

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

/** Release builds have no Network menu (docs/specs/architecture.md). */
@Suppress("UnusedReceiverParameter") // Same signature as the debug menu, which uses the RowScope.
@Composable
internal fun RowScope.TopBarActions() = Unit
