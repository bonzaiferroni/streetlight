package koala.component

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** Frames what [block] builds, usually a heading, between two decorative rules. */
fun FlowContent.filigree(
    mod: Modifier? = null,
    ruleMod: Modifier? = MaxWidth(16),
    block: DIV.() -> Unit
) {
    row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
        configureFiligree(ruleMod, block)
    }
}

/** Adds the rules of a [filigree] around what [block] builds. */
fun DIV.configureFiligree(
    ruleMaxWidth: Modifier? = MaxWidth(16),
    block: DIV.() -> Unit
) {
    val hrMods = modify(Flex1, MoonDropShadow, Height2Px, OpacityHalf, ParticleRay, AspectAuto, ruleMaxWidth)
    hr(modify(hrMods, FlipX, FiligreeStyle.RuleLeft)) {
        setRandomSeed()
    }
    block()
    hr(modify(hrMods, FiligreeStyle.RuleRight)) {
        setRandomSeed()
    }
}

object FiligreeStyle {
    val RuleLeft = Class("filigree-rule-left")
    val RuleRight = Class("filigree-rule-right")
}

// language="CSS"
val FiligreeCss get() = with(FiligreeStyle) { """
/* each rule slides in toward what it frames, by translate, which leaves the left rule's flip in place */
$RuleLeft, $RuleRight { transition: translate calc(var(--magic-interval) * 2) var(--magic-easing); }

$RuleLeft  { @starting-style { translate: -20px 0; } }
$RuleRight { @starting-style { translate:  20px 0; } }
""" }