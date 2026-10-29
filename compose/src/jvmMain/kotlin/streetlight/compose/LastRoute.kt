package streetlight.compose

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.serialization.json.Json
import streetlight.compose.io.readAppFile
import streetlight.compose.io.writeAppFile
import streetlight.compose.model.AppRoute
import streetlight.compose.model.Portal

/** Reads the route on top when the app last closed, or `null` when there is none or it cannot be read. */
suspend fun readLastRoute(): AppRoute? =
    readAppFile(ROUTE_FILE)?.let { runCatching { Json.decodeFromString(AppRoute.serializer(), it) }.getOrNull() }

/** Shows [route] as the app last left it: its own stack when it is a dock route, otherwise above home. */
fun Portal.restore(route: AppRoute) = when (route) {
    in stacks -> dock(route)
    else -> go(route)
}

/** Saves the route on top of this portal each time it changes, so the app reopens on it. */
suspend fun Portal.saveLastRoute() {
    snapshotFlow { backStack.last() }
        .drop(1)
        .distinctUntilChanged()
        .collect { writeAppFile(ROUTE_FILE, Json.encodeToString(AppRoute.serializer(), it)) }
}

private const val ROUTE_FILE = "route.json"
