package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.SvgFile
import koala.modifier.Glow
import koala.modifier.KoalaTheme
import streetlight.compose.model.ThemeEditor
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.shineBg
import streetlight.compose.utility.collectAsState

/**
 * The form of a page's theme: its colors and background mode, how it works, the field that places its lights, and each
 * light's color, energy, size and focus. The page shows the theme as it is edited. The cousin of the web's `themeForm`.
 */
@Composable
context(editor: ThemeEditor)
fun ThemeForm() {
    val theme = currentTheme
    val small = theme.text.small
    val edited by editor.themeState.collectAsState()
    PageThemeEffect(edited)

    FormCard(null) {
        ColumnRow {
            FormSection("Colors") {
                Row(1, Modifier.fillMaxWidth()) {
                    ColorPicker("accent", editor.accentState, Modifier.weight(1f))
                    ColorPicker("primary", editor.primaryState, Modifier.weight(1f))
                }
                CenteredText("Set the color of your theme and buttons.", small)
                BasicText(
                    "background mode",
                    Modifier.fillMaxWidth().filigree(),
                    style = theme.text.heading6.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
                SwitchMenu(editor.backgroundModeState, Modifier.fillMaxWidth())
                CenteredText("Flux makes the background colors slowly drift.", small)
            }
            FormSection("How it works") {
                Column(
                    1,
                    Modifier.fillMaxWidth().borderRadius(2, moonShadow = true, outline = Outline.Mid).shineBg().padding(2),
                ) {
                    CenteredText("The background of your page will be the same as what you see here.")
                    BasicText("Tips", style = theme.text.heading6, color = { theme.colors.ink })
                    CenteredText("For a single background color:", small)
                    FormBullets(
                        "Set one light to the largest size.",
                        "Turn the brightness of the other lights all the way down.",
                    )
                }
            }
            FormSection("Background") {
                GlowField(listOf(editor.rhoState, editor.betaState, editor.gammaState))
                CenteredText(
                    "Click and drag to change the position of the lights, and the background will change.",
                    small,
                )
            }
            FormSection("Glow Settings") {
                GlowSettings()
                CenteredText(
                    "You may change the color, size, brightness, and focus of the background lights.",
                    small,
                )
            }
        }
    }
}

/** A card for each light, two to a row, the third centered below. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
context(editor: ThemeEditor)
private fun GlowSettings() {
    val theme = currentTheme
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(theme.unit),
        maxItemsInEachRow = 2,
    ) {
        GlowCard("rho", editor.rhoState, Modifier.weight(1f))
        GlowCard("beta", editor.betaState, Modifier.weight(1f))
        GlowCard("gamma", editor.gammaState, Modifier.fillMaxWidth(0.5f))
    }
}

/**
 * The color of the light of [state], named [name], and sliders of its energy, size and focus. The web's `glowConfig`.
 */
@Composable
private fun GlowCard(name: String, state: MutableTap<Glow>, modifier: Modifier) {
    val theme = currentTheme
    val colorState = remember(state) { state.mutableTapOf({ it.color }) { copy(color = it) } }
    val energyState = remember(state) { state.mutableTapOf({ it.energy }) { copy(energy = it) } }
    val radiusState = remember(state) {
        state.mutableTapOf({ it.position.radius }) { copy(position = position.copy(radius = it)) }
    }
    val focusState = remember(state) { state.mutableTapOf({ it.focus }) { copy(focus = it) } }

    Column(1, modifier.widthIn(min = theme.unit * GlowCardUnits).borderRadius(2, outline = Outline.Mid).padding(1)) {
        ColorPicker(name, colorState, Modifier.fillMaxWidth())
        Slider(energyState, 0..KoalaTheme.MaxEnergy, icon = SvgFile.Bulb.small)
        Slider(radiusState, 0..KoalaTheme.MaxRadius, icon = SvgFile.Ruler.small)
        Slider(focusState, 0..KoalaTheme.MaxFocus, icon = SvgFile.Aperture.small)
    }
}

/** The least width of a light's card in units, the web's `MinWidth(24)`. */
private const val GlowCardUnits = 24
