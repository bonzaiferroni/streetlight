package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import kotlinx.html.DIV
import streetlight.model.utils.AddedText
import streetlight.model.utils.CommonText
import streetlight.model.utils.RemovedText
import streetlight.model.utils.createTextDelta

fun TagScope.deltaGrid(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) {
    grid(columnsOf(LinearDimension.auto, 1.fr), mod = mod) {
        content()
    }
}

fun TagScope.deltaRow(
    label: String,
    value: Any?,
    previousValue: Any? = null,
) {
    strong(label)
    val valueText = value?.toString()
    val previousValueText = previousValue?.toString()
    val delta = createTextDelta(previousValueText, valueText)

    textBlock(mod = modify(WhiteSpacePreLine)) {
        delta.segments.forEach { segment ->
            val mod = when (segment) {
                is CommonText -> TextDeltaStyle.CommonText
                is AddedText -> TextDeltaStyle.AddedText
                is RemovedText -> TextDeltaStyle.RemovedText
            }
            span(segment.text, modify(mod))
        }
    }
}

internal fun getValue(value: Any) = value.toString()