package koala.html

import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.JustifyContentCenter
import koala.css.MaxWidth16
import koala.css.Modifier
import koala.css.ModifierSet
import koala.css.OpacityLow
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.hr

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
    val hrMods = modify(Flex1, OpacityLow, ruleMaxWidth)
    hr {
        addModifiers(hrMods)
    }
    block()
    hr {
        addModifiers(hrMods)
    }
}