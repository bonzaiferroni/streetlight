package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.mutableTapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import streetlight.web.utils.localStoreOf

/** The viewer's site settings, kept in local storage. */
class SiteConfig(
    private val scope: CoroutineScope
) {
    private val state = localStoreOf(scope, SITE_CONFIG_KEY, SiteConfigState())

    val stateNow get() = state.now
    val stateFlow = state.flow
    val showTransitState = state.mutableTapOf({ it.showTransit }) { copy(showTransit = it) }
    val themeFlow = state.mutableTapOf({ it.theme }) { copy(theme = it) }
    val locationPostModeState = state.mutableTapOf({ it.locationPostMode }) { copy(locationPostMode = it) }
    val eventPostModeState = state.mutableTapOf({ it.eventPostMode }) { copy(eventPostMode = it) }
}

@Serializable
data class SiteConfigState(
    val showTransit: Boolean = false,
    val theme: SiteTheme = SiteTheme.Dark,
    val locationPostMode: LocationPostMode = LocationPostMode.Return,
    val eventPostMode: EventPostMode = EventPostMode.Return,
)

private const val SITE_CONFIG_KEY = "streetlight.site-config"

enum class SiteTheme: Labeled {
    Dark,
    Light;

    override val label get() = name
}

/** What the location scout does after a post. */
@Serializable
enum class LocationPostMode(override val label: String): Labeled {
    ResetLocation("post and find another location"),
    Return("post and return"),
}

/** What the event scout does after a post. */
@Serializable
enum class EventPostMode(override val label: String): Labeled {
    ResetLocation("post and find another location"),
    ResetEvent("post another event here"),
    Return("post and return"),
}
