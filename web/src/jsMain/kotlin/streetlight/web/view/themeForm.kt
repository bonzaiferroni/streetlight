package streetlight.web.view

import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.component.GridColumns
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.component.bulletsOf
import streetlight.model.data.PageTheme
import streetlight.web.model.ThemeEditor
import koala.dom.component.*
import koala.dom.markdown.markdown
import koala.markdown.lazyMarkdown
import streetlight.web.dom.page.applyTheme
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.component.slider

context(model: ThemeEditor)
fun ViewScope.themeForm() {
    formCard {
        formRow {
            formSection("Colors") {
                row(FlexItems1) {
                    colorPicker("accent", model.accentState)
                    colorPicker("primary", model.primaryState)
                }
                row(FlexItems1) {
                    switch("color flux", model.colorFluxState)
                    centeredText("Color Flux makes the background colors slowly drift.", TextSmall)
                }
            }
            formSection("How it works") {
                markdown(tipsInfo)
            }

            formSection("Background") {
                box(modify(VoidBg, BorderRadius2, Outline, OverflowClip, MoonShadow)) {
                    glowField(model.rhoState)
                    glowField(model.betaState)
                    glowField(model.gammaState)
                }
                centeredText("Click and drag to change the position of the lights, and the background will change.", TextSmall)
            }

            formSection("Glow Settings") {
                column(modify(ZenGrid, BorderRadius1, OverflowClip)) {
                    glowConfig("rho", model.rhoState)
                    glowConfig("beta", model.betaState)
                    glowConfig("gamma", model.gammaState)
                }
                centeredText("You may change the color, size, brightness, and focus of the background lights.", TextSmall)
            }
        }
    }

    applyThemeState(model.themeState)
}

fun ViewScope.glowConfig(name: String, state: MutableTap<Glow>) {
    val colorState = state.mutableTapOf({ it.color }) { copy(color = it) }
    val radiusState = state.mutableTapOf({ it.position.radius }) { copy(position = position.copy(radius = it)) }
    val energyState = state.mutableTapOf({ it.energy }) { copy(energy = it) }
    val focusState = state.mutableTapOf({ it.focus }) { copy(focus = it) }

    grid(GridColumns.repeatFill(192), modify(Padding(1))) {
        colorPicker(name, colorState)
        slider(SvgFile.Ruler.small, radiusState, (0..KoalaTheme.MaxRadius))
        slider(SvgFile.Bulb.small, energyState, (0..KoalaTheme.MaxEnergy))
        slider(SvgFile.Aperture.small, focusState, (0..KoalaTheme.MaxFocus))
    }
}

fun ViewScope.applyThemeState(state: MutableTap<PageTheme>) {
    launchEffect {
        state.flow.collect {
            applyTheme(it)
        }
    }
}

//language="MD"
private val tipsInfo by lazyMarkdown("""
The background of your page will be the same as what you see here.
    
##### Tips ---
For a single background color: 
* Set one light to the largest size. 
* Turn the brightness on the other lights all the way down.

To remove all background colors, turn the brightness on each light all the way down.
""")