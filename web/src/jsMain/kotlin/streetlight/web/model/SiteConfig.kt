package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import koala.interop.applyRootStyle
import koala.interop.setRootModifier
import koala.modifier.Class
import koala.modifier.DayTheme
import koala.modifier.toCssText
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import streetlight.model.data.PageTheme
import streetlight.web.page.DEFAULT_THEME_KEY
import streetlight.web.page.DefaultThemeOnly
import streetlight.web.page.toStyleSet
import streetlight.web.utility.localStoreOf
import web.cssom.ClassName
import web.dom.document

/** The viewer's site settings, kept in local storage. */
class SiteConfig(
    private val scope: CoroutineScope
) {
    private val state = localStoreOf(scope, SITE_CONFIG_KEY, SiteConfigState())
    val pageThemeState = localStoreOf(scope, "default-theme", PageTheme())
    val defaultThemeOnlyState = storeOf(hasRootModifier(DefaultThemeOnly))
    val themeState = storeOf(if (hasRootModifier(DayTheme)) SiteTheme.Light else SiteTheme.Dark)

    val stateNow get() = state.now
    val stateFlow = state.flow
    val showTransitState = state.mutableTapOf({ it.showTransit }) { copy(showTransit = it) }
    val locationPostModeState = state.mutableTapOf({ it.locationPostMode }) { copy(locationPostMode = it) }
    val eventPostModeState = state.mutableTapOf({ it.eventPostMode }) { copy(eventPostMode = it) }

    init {
        pageThemeState.reactIn(scope) { applyRootStyle(DEFAULT_THEME_KEY, it.toStyleSet().toCssText()) }
        defaultThemeOnlyState.reactIn(scope) { setRootModifier(DefaultThemeOnly.identifier, it) }
        themeState.reactIn(scope) { setRootModifier(DayTheme.identifier, it == SiteTheme.Light) }
    }
}

private fun hasRootModifier(mod: Class) = document.documentElement.classList.contains(ClassName(mod.identifier))

@Serializable
data class SiteConfigState(
    val showTransit: Boolean = false,
    val locationPostMode: LocationPostMode = LocationPostMode.Return,
    val eventPostMode: EventPostMode = EventPostMode.Return,
)

private const val SITE_CONFIG_KEY = "streetlight.site-config"

enum class SiteTheme: Labeled {
    Dark,
    Light;

    override val label get() = name
}

/** The location scout's action after a post. */
@Serializable
enum class LocationPostMode(override val label: String): Labeled {
    ResetLocation("post and find another location"),
    Return("post and return"),
}

/** The event scout's action after a post. */
@Serializable
enum class EventPostMode(override val label: String): Labeled {
    ResetLocation("post and find another location"),
    ResetEvent("post another event here"),
    Return("post and return"),
}
