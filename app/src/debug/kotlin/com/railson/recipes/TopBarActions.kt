package com.railson.recipes

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.network.fake.FakeNetworkConfig
import com.railson.recipes.core.network.fake.FakeNetworkMode
import com.railson.recipes.core.ui.theme.Spacing
import org.koin.compose.koinInject

/** Debug builds only: the ⋮ "Network" menu that switches the fake network mode (docs/specs/architecture.md). */
@Composable
internal fun RowScope.TopBarActions() {
    val config = koinInject<FakeNetworkConfig>()
    var expanded by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(config.mode) }
    IconButton(onClick = { expanded = true }) {
        Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.network_menu))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        Text(
            text = stringResource(R.string.network_menu),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
        )
        FakeNetworkMode.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(stringResource(option.label)) },
                leadingIcon = { RadioButton(selected = option == mode, onClick = null) },
                onClick = {
                    config.mode = option
                    mode = option
                    expanded = false
                },
            )
        }
    }
}

private val FakeNetworkMode.label: Int
    get() = when (this) {
        FakeNetworkMode.Normal -> R.string.network_mode_normal
        FakeNetworkMode.Slow -> R.string.network_mode_slow
        FakeNetworkMode.EmptyCatalogue -> R.string.network_mode_empty_catalogue
        FakeNetworkMode.NoNetwork -> R.string.network_mode_no_network
        FakeNetworkMode.Timeout -> R.string.network_mode_timeout
        FakeNetworkMode.RateLimited -> R.string.network_mode_rate_limited
        FakeNetworkMode.ServerError -> R.string.network_mode_server_error
        FakeNetworkMode.Unavailable -> R.string.network_mode_unavailable
        FakeNetworkMode.BadData -> R.string.network_mode_bad_data
    }
