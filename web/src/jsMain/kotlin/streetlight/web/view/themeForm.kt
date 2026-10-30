package streetlight.web.view

import koala.SvgFile
import kotlinx.css.LinearDimension
import koala.modifier.*
import koala.dom.*
import koala.component.GridColumns
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.component.bulletsOf
import koala.component.heading6
import streetlight.model.data.PageTheme
import streetlight.web.model.ThemeEditor
import koala.dom.component.*
import koala.dom.markdown.markdown
import koala.markdown.lazyMarkdown
import kotlinx.css.pct
import streetlight.web.dom.page.applyTheme
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.component.slider
import kotlin.math.roundToInt

context(model: ThemeEditor)
fun ViewScope.themeForm() {
    formCard {
        formRow {
            formSection("Colors") {
                row(FlexItems1) {
                    colorPicker("accent", model.accentState)
                    colorPicker("primary", model.primaryState)
                }
                centeredText("Set the color of your theme and buttons.", TextSmall)

                filigree(MarginTop(1)) {
                    heading6("background mode")
                }
                switchMenu(model.backgroundModeState)
                centeredText("Flux makes the background colors slowly drift.", TextSmall)
            }
            formSection("How it works") {
                card(modify(ShineBg, Padding(2), Outline, MoonShadow)) {
                    markdown(tipsInfo)
                }
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
                row(modify(FlexWrap, JustifyContentCenter)) {
                    glowConfig("rho", model.rhoState)
                    glowConfig("beta", model.betaState)
                    glowConfig("gamma", model.gammaState)
                }
                centeredText("You may change the color, size, brightness, and focus of the background lights.", TextSmall)
            }
            formSection("Curve") {
                val curveState = model.curveState.mutableTapOf({ (it * 100).roundToInt() }) { it / 100f }
                slider(SvgFile.WaveSine.small, curveState, (0..100))
                centeredText("Set the curve of your corners.", TextSmall)
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

    // half the row less half its gap, so two share a line and the third centers below
    card(modify(MinWidth(24), Outline, Css.Width.of(LinearDimension("calc((100% - var(--unit)) / 2)")))) {
        colorPicker(name, colorState)
        slider(SvgFile.Bulb.small, energyState, (0..KoalaTheme.MaxEnergy))
        slider(SvgFile.Ruler.small, radiusState, (0..KoalaTheme.MaxRadius))
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
    
###### Tips ---
For a single background color: 
1. Set one light to the largest size. 
2. Turn the brightness of the other lights all the way down.
""")
