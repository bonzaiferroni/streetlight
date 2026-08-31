package streetlight.web.model

import koala.css.Koala
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.PageTheme

class ThemeEditor(initialTheme: PageTheme?) {
    private val state = storeOf(initialTheme ?: PageTheme())
    val themeState: MutableTap<PageTheme> = state

    val accentState = state.mutableTapOf({ it.accent }) { copy(accent = it) }
    val primaryState = state.mutableTapOf({ it.primary }) { copy(primary = it) }
    val rhoState = state.mutableTapOf({ it.rho }) { copy(rho = it) }
    val betaState = state.mutableTapOf({ it.beta }) { copy(beta = it) }
    val gammaState = state.mutableTapOf({ it.gamma }) { copy(gamma = it) }
    val colorFluxState = state.mutableTapOf({ it.colorFlux }) { copy(colorFlux = it) }

    fun buildTheme(): PageTheme? {
        return state.now.takeIf { it != Koala }
    }
}
