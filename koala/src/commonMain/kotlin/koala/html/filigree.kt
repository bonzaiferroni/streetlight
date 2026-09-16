package koala.html

import koala.modifier.AlignItemsCenter
import koala.modifier.AspectAuto
import koala.modifier.Flex1
import koala.modifier.FlipX
import koala.modifier.Height2Px
import koala.modifier.JustifyContentCenter
import koala.modifier.MaxWidth16
import koala.modifier.Modifier
import koala.modifier.ModifierSet
import koala.modifier.MoonDropShadow
import koala.modifier.OpacityHalf
import koala.modifier.ParticleRay
import koala.modifier.modify
import koala.modifier.setRandomSeed
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
    hr(modify(hrMods, FlipX)) {
        setRandomSeed()
    }
    block()
    hr(hrMods) {
        setRandomSeed()
    }
}