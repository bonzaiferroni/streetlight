package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** Frames what [block] builds, usually a heading, between two decorative rules. */
fun FlowContent.filigree(
    mod: Modifier? = null,
    ruleMod: Modifier = MaxWidth(16),
    block: DIV.() -> Unit
) {
    row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
        configureFiligree(ruleMod, block)
    }
}

/** Adds the rules of a [filigree] around what [block] builds. */
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