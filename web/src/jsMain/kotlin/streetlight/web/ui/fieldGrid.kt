package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import kotlinx.html.DIV
import streetlight.model.utils.AddedText
import streetlight.model.utils.CommonText
import streetlight.model.utils.RemovedText
import streetlight.model.utils.TextDelta
import streetlight.model.utils.TextDeltaDisplay
import streetlight.model.utils.createTextDelta

fun TagScope.fieldGrid(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) {
    grid(columnsOf(LinearDimension.auto, 1.fr, LinearDimension.auto), mod = mod) {
        content()
    }
}

fun TagScope.fieldValue(
    value: Any?,
    previousValue: Any?,
    label: String
) {
    strong(label)
    val valueText = value?.toString()
    val previousValueText = previousValue?.toString()
    val delta = createTextDelta(previousValueText, valueText)
    val swap = swap {
        valueCell(delta, TextDeltaDisplay.New)
        valueCell(delta, TextDeltaDisplay.Old)
    }
    button(onClick = swap::next) {
        +"swap"
    }
}

fun TagScope.valueCell(delta: TextDelta, display: TextDeltaDisplay) {
    textBlock(mod = modify(WhiteSpacePreLine)) {
        delta.segments.forEach { segment ->
            when {
                segment is CommonText -> span(segment.text)
                segment is AddedText && display != TextDeltaDisplay.Old -> span(segment.text, modify(PrimaryFg))
                segment is RemovedText && display != TextDeltaDisplay.New -> span(segment.text, modify(AccentFg))
            }
        }
    }
}

internal fun getValue(value: Any) = value.toString()