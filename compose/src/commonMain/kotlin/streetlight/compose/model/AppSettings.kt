package streetlight.compose.model

import kampfire.model.Labeled
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.compose.view.FeedMode

/** The viewer's settings that hold across the app, as the web holds its root switches. */
class AppSettings {
    private val store = storeOf(AppSettingsState())

    val feedModeState = store.mutableTapOf({ it.feedMode }) { copy(feedMode = it) }
    val locationPostModeState = store.mutableTapOf({ it.locationPostMode }) { copy(locationPostMode = it) }
}

/** The state of [AppSettings]. */
data class AppSettingsState(
    val feedMode: FeedMode = FeedMode.Minimal,
    val locationPostMode: LocationPostMode = LocationPostMode.Return,
)

/** The location scout's action after a post. */
enum class LocationPostMode(override val label: String) : Labeled {
    ResetLocation("post and find another location"),
    Return("post and return"),
}
