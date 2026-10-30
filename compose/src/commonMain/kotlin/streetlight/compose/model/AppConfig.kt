package streetlight.compose.model

import kampfire.model.Labeled
import kampfire.model.mutableTapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import streetlight.compose.io.localStoreOf
import streetlight.compose.view.FeedMode
import streetlight.model.data.PageTheme

/**
 * The viewer's settings that hold across the app, kept in the app's files from [scope], as the web keeps its root
 * switches in local storage.
 */
class AppConfig(scope: CoroutineScope) {
    private val store = localStoreOf(scope, ConfigFile, AppConfigState())

    val feedModeState = store.mutableTapOf({ it.feedMode }) { copy(feedMode = it) }
    val locationPostModeState = store.mutableTapOf({ it.locationPostMode }) { copy(locationPostMode = it) }
    val eventPostModeState = store.mutableTapOf({ it.eventPostMode }) { copy(eventPostMode = it) }
    val isDefaultThemeOnlyState = store.mutableTapOf({ it.isDefaultThemeOnly }) { copy(isDefaultThemeOnly = it) }
    val defaultThemeState = store.mutableTapOf({ it.defaultTheme }) { copy(defaultTheme = it) }
}

@Serializable
data class AppConfigState(
    val feedMode: FeedMode = FeedMode.Minimal,
    val locationPostMode: LocationPostMode = LocationPostMode.Return,
    val eventPostMode: EventPostMode = EventPostMode.Return,
    val isDefaultThemeOnly: Boolean = false,
    val defaultTheme: PageTheme = PageTheme(),
)

private const val ConfigFile = "config.json"

/** The location scout's action after a post. */
enum class LocationPostMode(override val label: String) : Labeled {
    ResetLocation("post and find another location"),
    Return("post and return"),
}

/** The event scout's action after a post. */
enum class EventPostMode(override val label: String) : Labeled {
    ResetLocation("post and find another location"),
    ResetEvent("post another event here"),
    Return("post and return"),
}
