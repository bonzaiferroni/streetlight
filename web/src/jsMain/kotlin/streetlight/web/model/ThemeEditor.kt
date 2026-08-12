package streetlight.web.model

import kampfire.model.Vector2
import koala.css.GlowPosition
import koala.css.Koala
import koala.model.MutableTap
import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.PageTheme
import kotlin.collections.copy
import kotlin.math.roundToInt

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
