package streetlight.web.model

import kampfire.model.Vector2
import koala.css.CirclePosition
import koala.css.Koala
import koala.model.MutableField
import koala.model.mutableFieldOf
import koala.model.storeOf
import streetlight.model.data.PageTheme
import kotlin.math.roundToInt

class ThemeEditor(initialTheme: PageTheme?) {
    private val state = storeOf(initialTheme ?: PageTheme())
    val themeState: MutableField<PageTheme> = state

    val accentState = state.mutableFieldOf({ it.accent }) { copy(accent = it) }
    val primaryState = state.mutableFieldOf({ it.primary }) { copy(primary = it) }
    val rhoState = state.mutableFieldOf({ it.rho }) { copy(rho = it) }
    val betaState = state.mutableFieldOf({ it.beta }) { copy(beta = it) }
    val gammaState = state.mutableFieldOf({ it.gamma }) { copy(gamma = it) }
    val rhoColorState = rhoState.mutableFieldOf({ it.color }) { copy(color = it) }
    val betaColorState = betaState.mutableFieldOf({ it.color }) { copy(color = it) }
    val gammaColorState = gammaState.mutableFieldOf({ it.color }) { copy(color = it) }
    val rhoRadiusState = rhoState.mutableFieldOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }
    val betaRadiusState = betaState.mutableFieldOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }
    val gammaRadiusState = gammaState.mutableFieldOf({ it.position.radius ?: 40 }) { copy(position = position.copy(radius = it)) }

    fun buildTheme(): PageTheme? {
        return state.now.takeIf { it != Koala }
    }
}

private fun CirclePosition.toVector2() = Vector2(x.toDouble(), y.toDouble())

private fun CirclePosition.withVector2(vector: Vector2) = copy(
    x = vector.x.roundToInt(),
    y = vector.y.roundToInt(),
)