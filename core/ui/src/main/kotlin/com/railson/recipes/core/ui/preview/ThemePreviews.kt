package com.railson.recipes.core.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** A preview in light and in dark. Every stateless composable uses it (docs/guidelines.md, Compose). */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
