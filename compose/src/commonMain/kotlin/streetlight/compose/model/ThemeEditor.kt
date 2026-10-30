package streetlight.compose.model

import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.PageTheme

/** The edit of a page's theme: its colors, its three glows, its background mode, and its curve. The web's `ThemeEditor`. */
class ThemeEditor(initialTheme: PageTheme) {
    private val store = storeOf(initialTheme)

    val themeState: MutableTap<PageTheme> = store
    val accentState = store.mutableTapOf({ it.accent }) { copy(accent = it) }
    val primaryState = store.mutableTapOf({ it.primary }) { copy(primary = it) }
    val rhoState = store.mutableTapOf({ it.rho }) { copy(rho = it) }
    val betaState = store.mutableTapOf({ it.beta }) { copy(beta = it) }
    val gammaState = store.mutableTapOf({ it.gamma }) { copy(gamma = it) }
    val backgroundModeState = store.mutableTapOf({ it.backgroundMode }) { copy(backgroundMode = it) }
    val curveState = store.mutableTapOf({ it.curve }) { copy(curve = it) }
}
