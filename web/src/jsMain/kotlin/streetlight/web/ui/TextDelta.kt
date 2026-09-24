package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import kotlinx.html.DIV
import streetlight.model.utils.AddedText
import streetlight.model.utils.CommonText
import streetlight.model.utils.RemovedText
import streetlight.model.utils.createTextDelta

/** A grid of [deltaRow]s. */
fun AppendScope.deltaGrid(
    mod: Modifier? = null,
    content: DIV.() -> Unit
) {
    grid(columnsOf(LinearDimension.auto, 1.fr), mod = mod) {
        content()
    }
}

/** [label] and the text of [value], marked with what was added and removed since [previousValue]. */
fun AppendScope.deltaRow(
    label: String,
    value: Any?,
    previousValue: Any? = null,
) {
    strong(label)
    val valueText = value?.toString()
    val previousValueText = previousValue?.toString()
    val delta = createTextDelta(previousValueText, valueText)

    textBlock(mod = WhiteSpacePreLine) {
        delta.segments.forEach { segment ->
            val mod = when (segment) {
                is CommonText -> TextDeltaStyle.CommonText
                is AddedText -> TextDeltaStyle.AddedText
                is RemovedText -> TextDeltaStyle.RemovedText
            }
            span(segment.text, mod)
        }
    }
}

internal fun getValue(value: Any) = value.toString()