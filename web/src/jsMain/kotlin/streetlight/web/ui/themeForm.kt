package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.MutableTap
import koala.model.mutableTapOf
import kotlinx.browser.document
import kotlinx.css.pct
import kotlinx.css.vh
import streetlight.model.data.PageTheme
import streetlight.web.model.ThemeEditor

fun ViewScope.themeForm(model: ThemeEditor) {
    formColumn {
        formRow {
            formSection("Colors") {
                row(modify(FlexItems1)) {
                    colorPicker("accent", model.accentState)
                    colorPicker("primary", model.primaryState)
                }
                row(modify(FlexItems1)) {
                    switch("color flux", model.colorFluxState)
                    formText("Color Flux makes the background colors slowly drift.")
                }
            }
            formSection("Background") {
                box(modify(VoidBg, BorderRadius2, OutlineSolid2Px, OverflowClip)) {
                    glowField(model.rhoState)
                    glowField(model.betaState)
                    glowField(model.gammaState)
                }
                formText("Click and drag to change the position of background glows.")
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

    row(modify(FlexItems1)) {
        colorPicker(name, colorState)
        slider(SvgFile.Ruler, radiusState)
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