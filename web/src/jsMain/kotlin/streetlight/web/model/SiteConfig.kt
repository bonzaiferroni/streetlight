package streetlight.web.model

import kampfire.model.Labeled
import koala.model.dedup
import koala.model.mutableFieldOf
import koala.model.storeOf
import koala.utils.jsonConfig
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import org.w3c.dom.get
import org.w3c.dom.set

class SiteConfig {
    private val state = storeOf(readStateFromLocalStorage() ?: SiteConfigState())

    val stateNow get() = state.now
    val stateFlow = state.flow
    val showTransitFlow = state.flow.dedup { it.showTransit }
    val themeFlow = state.mutableFieldOf({ it.theme }) { copy(theme = it) }

    fun setShowTransit(value: Boolean) {
        setState { it.copy(showTransit = value) }
    }

    private fun setState(mutate: (SiteConfigState) -> SiteConfigState) {
        val newState = mutate(stateNow)
        state.setValue { newState }
        writeStateToLocalStorage(newState)
    }

    private fun readStateFromLocalStorage(): SiteConfigState? = localStorage[SITE_CONFIG_KEY]?.let {
        jsonConfig.decodeFromString(it)
    }

    private fun writeStateToLocalStorage(state: SiteConfigState) {
        localStorage[SITE_CONFIG_KEY] = jsonConfig.encodeToString(state)
    }
}

@Serializable
data class SiteConfigState(
    val showTransit: Boolean = false,
    val theme: SiteTheme = SiteTheme.Dark,
)

private const val SITE_CONFIG_KEY = "streetlight.site-config"

enum class SiteTheme: Labeled {
    Dark,
    Light;

    override val label get() = name
}