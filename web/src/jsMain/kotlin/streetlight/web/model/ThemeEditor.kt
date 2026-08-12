package streetlight.web.model

import kampfire.model.Vector2
import koala.css.CirclePosition
import koala.css.Koala
import koala.model.MutableTap
import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.PageTheme
import kotlin.math.roundToInt

class ThemeEditor(initialTheme: PageTheme?) {
    private val state = storeOf(initialTheme ?: PageTheme())
    val themeState: MutableTap<PageTheme> = state

    val accentState = state.mutableTapOf({ it.accent }) { copy(accent = it) }
    val primaryState = state.mutableTapOf({ it.primary }) { copy(primary = it) }
    val rhoState = state.mutableTapOf({ it.rho }) { copy(rho = it) }
    val betaState = state.mutableTapOf({ it.beta }) { copy(beta = it) }
    val gammaState = state.mutableTapOf({ it.gamma }) { copy(gamma = it) }
    val rhoColorState = rhoState.mutableTapOf({ it.color }) { copy(color = it) }
    val betaColorState = betaState.mutableTapOf({ it.color }) { copy(color = it) }
    val gammaColorState = gammaState.mutableTapOf({ it.color }) { copy(color = it) }
    val rhoRadiusState = rhoState.mutableTapOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }
    val betaRadiusState = betaState.mutableTapOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }
    val gammaRadiusState = gammaState.mutableTapOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }

    fun buildTheme(): PageTheme? {
        return state.now.takeIf { it != Koala }
    }
}

private fun CirclePosition.toVector2() = Vector2(x.toDouble(), y.toDouble())

private fun CirclePosition.withVector2(vector: Vector2) = copy(
    x = vector.x.roundToInt(),
    y = vector.y.roundToInt(),
)