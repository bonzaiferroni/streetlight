package streetlight.web.ui

import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import streetlight.model.data.PageTheme
import streetlight.web.model.ThemeEditor

fun ViewScope.themeForm(model: ThemeEditor) {
    formCard {
        formRow {
            formSection("Colors") {
                row(FlexItems1) {
                    colorPicker("accent", model.accentState)
                    colorPicker("primary", model.primaryState)
                }
                row(FlexItems1) {
                    switch("color flux", model.colorFluxState)
                    centeredText("Color Flux makes the background colors slowly drift.")
                }
            }
            formSection("Background") {
                box(modify(VoidBg, BorderRadius2, Outline, OverflowClip)) {
                    glowField(model.rhoState)
                    glowField(model.betaState)
                    glowField(model.gammaState)
                }
                centeredText("Click and drag to change the position of background glows.")
            }
        }
        formSection("Glow Settings") {
            glowConfig("rho", model.rhoState)
            glowConfig("beta", model.betaState)
            glowConfig("gamma", model.gammaState)
        }
    }

    applyThemeState(model.themeState)
}

fun ViewScope.glowConfig(name: String, state: MutableTap<Glow>) {
    val colorState = state.mutableTapOf({ it.color }) { copy(color = it) }
    val radiusState = state.mutableTapOf({ it.position.radius }) { copy(position = position.copy(radius = it)) }
    val energyState = state.mutableTapOf({ it.energy }) { copy(energy = it) }
    val focusState = state.mutableTapOf({ it.focus }) { copy(focus = it) }

    row(FlexItems1) {
        colorPicker(name, colorState)
        slider(SvgFile.Ruler, radiusState, (0..KoalaTheme.MaxRadius))
        slider(SvgFile.Bulb, energyState, (0..KoalaTheme.MaxEnergy))
        slider(SvgFile.Aperture, focusState, (0..KoalaTheme.MaxFocus))
    }
}

fun ViewScope.applyThemeState(state: MutableTap<PageTheme>) {
    launchEffect {
        state.flow.collect {
            applyTheme(it)
        }
    }
}