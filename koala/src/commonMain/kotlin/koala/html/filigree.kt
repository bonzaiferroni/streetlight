package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.filigree(
    modifiers: ModifierSet? = null,
    ruleMaxWidth: Modifier = MaxWidth(16),
    block: DIV.() -> Unit
) {
    row(modify(modifiers, JustifyContentCenter, AlignItemsCenter)) {
        configureFiligree(ruleMaxWidth, block)
    }
}

fun DIV.configureFiligree(
    ruleMaxWidth: Modifier = MaxWidth(16),
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