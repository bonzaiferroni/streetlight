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
    val postAndResetState = state.mutableTapOf({ it.postAndReset }) { copy(postAndReset = it) }
}

@Serializable
data class SiteConfigState(
    val showTransit: Boolean = false,
    val theme: SiteTheme = SiteTheme.Dark,
    val postAndReset: Boolean = false,
)

private const val SITE_CONFIG_KEY = "streetlight.site-config"

enum class SiteTheme: Labeled {
    Dark,
    Light;

    override val label get() = name
}