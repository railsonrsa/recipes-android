package com.railson.recipes.core.network.fake

/** How the fake network behaves. See docs/specs/data.md, "Fake network modes". */
enum class FakeNetworkMode {
    Normal,
    Slow,
    EmptyCatalogue,
    NoNetwork,
    Timeout,
    RateLimited,
    ServerError,
    Unavailable,
    BadData,
}

/** The current fake network mode. Lives in memory, so the app starts in Normal again. */
class FakeNetworkConfig {
    @Volatile
    var mode: FakeNetworkMode = FakeNetworkMode.Normal
}
