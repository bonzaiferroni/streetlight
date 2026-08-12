package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.MutableTap
import kotlinx.browser.document
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
                row(modify(FlexItems1)) {
                    colorPicker("light 1", model.rhoColorState)
                    slider(model.rhoRadiusState, (0..100))
                }
                box(modify()) {
                    lightControl(model.rhoState)
                    lightControl(model.betaState)
                    lightControl(model.gammaState)
                }

                colorPicker("light 2", model.betaColorState)
                colorPicker("light 3", model.gammaColorState)
            }
        }
    }

    applyThemeState(model.themeState)
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
    body.setStyle(KoalaStyle.RhoColor.to(theme.rho.color))
    body.setStyle(KoalaStyle.BetaColor.to(theme.beta.color))
    body.setStyle(KoalaStyle.GammaColor.to(theme.gamma.color))
    body.setStyle(KoalaStyle.RhoPosition.to(theme.rho.position))
    body.setStyle(KoalaStyle.BetaPosition.to(theme.beta.position))
    body.setStyle(KoalaStyle.GammaPosition.to(theme.gamma.position))
}