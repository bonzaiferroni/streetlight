package koala.html

import koala.css.AlignItemsCenter
import koala.css.AspectAuto
import koala.css.Flex1
import koala.css.FlipX
import koala.css.Height2Px
import koala.css.JustifyContentCenter
import koala.css.MaxWidth16
import koala.css.Modifier
import koala.css.ModifierSet
import koala.css.MoonDropShadow
import koala.css.OpacityHalf
import koala.css.ParticleRay
import koala.css.modify
import koala.css.setRandomSeed
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.filigree(
    modifiers: ModifierSet? = null,
    ruleMaxWidth: Modifier = MaxWidth16,
    block: DIV.() -> Unit
) {
    row(modify(modifiers, JustifyContentCenter, AlignItemsCenter)) {
        configureFiligree(ruleMaxWidth, block)
    }
}

fun DIV.configureFiligree(
    ruleMaxWidth: Modifier = MaxWidth16,
    block: DIV.() -> Unit
) {
    val hrMods = modify(Flex1, MoonDropShadow, Height2Px, OpacityHalf, ParticleRay, AspectAuto, ruleMaxWidth)
    hr(hrMods + FlipX) {
        setRandomSeed()
    }
    block()
    hr(hrMods) {
        setRandomSeed()
    }
}