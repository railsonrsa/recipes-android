package com.railson.recipes.recipes.domain.model

/** How a call to the server ended. See docs/specs/data.md, "Call outcomes". */
enum class CallOutcome {
    Success,
    NoConnection,
    Timeout,
    BadRequest,
    NotFound,
    RateLimited,
    ServerError,
    Unavailable,
    BadData,
}
