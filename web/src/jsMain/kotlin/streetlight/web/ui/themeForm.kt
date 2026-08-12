package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.MutableTap
import koala.model.mutableTapOf
import kotlinx.browser.document
import kotlinx.css.pct
import kotlinx.css.vw
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
            }
            formSection("Background") {
                box(modify(VoidBg, BorderRadius2, OutlineSolid2Px, OverflowClip)) {
                    glowControl(model.rhoState)
                    glowControl(model.betaState)
                    glowControl(model.gammaState)
                }
            }
        }
        formSection("Glow") {
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

fun applyTheme(theme: PageTheme) {
    val body = document.body ?: return
    body.setStyle(KoalaStyle.Accent.to(theme.accent))
    body.setStyle(KoalaStyle.Primary.to(theme.primary))
    body.setStyle(KoalaStyle.RhoColor.to(theme.rho.rgba()))
    body.setStyle(KoalaStyle.BetaColor.to(theme.beta.rgba()))
    body.setStyle(KoalaStyle.GammaColor.to(theme.gamma.rgba()))
    body.setStyle(KoalaStyle.RhoX.to(theme.rho.position.x.pct))
    body.setStyle(KoalaStyle.BetaX.to(theme.beta.position.x.pct))
    body.setStyle(KoalaStyle.GammaX.to(theme.gamma.position.x.pct))
    body.setStyle(KoalaStyle.RhoY.to(theme.rho.position.y.pct))
    body.setStyle(KoalaStyle.BetaY.to(theme.beta.position.y.pct))
    body.setStyle(KoalaStyle.GammaY.to(theme.gamma.position.y.pct))
    body.setStyle(KoalaStyle.RhoRadius.to(theme.rho.position.radius.vw))
    body.setStyle(KoalaStyle.BetaRadius.to(theme.beta.position.radius.vw))
    body.setStyle(KoalaStyle.GammaRadius.to(theme.gamma.position.radius.vw))
    body.setStyle(KoalaStyle.RhoFocus.to(theme.rho.focus.pct))
    body.setStyle(KoalaStyle.BetaFocus.to(theme.beta.focus.pct))
    body.setStyle(KoalaStyle.GammaFocus.to(theme.gamma.focus.pct))
}