package koala.dom

import koala.SvgFile
import koala.css.*
import koala.html.heading3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.enums.enumEntries

inline fun <reified State: Enum<State>> RenderContext.stageBlock(
    flow: Flow<State>,
    crossinline onValue: (State) -> Unit,
    modifiers: ModifierSet? = null,
    noinline block: RenderContext.(State) -> Unit
): HTMLDivElement {
    val entries = enumEntries<State>()
    var currentValue: State? = null
    val elements = mutableMapOf<State, HTMLElement>()

    val selectElement: (State) -> Unit = { value ->
        if (value != currentValue) {
            currentValue = value
            val currentOrdinal = currentValue.ordinal
            elements.forEach { (value, element) ->
                when  {
                    value.ordinal < currentOrdinal -> {
                        element.modify(Clickable)
                        element.unmodify(Selected)
                        element.unmodify(Dim)
                    }
                    value.ordinal == currentOrdinal -> {
                        element.unmodify(Clickable)
                        element.modify(Selected)
                        element.unmodify(Dim)
                    }
                    value.ordinal > currentOrdinal -> {
                        element.unmodify(Clickable)
                        element.unmodify(Selected)
                        element.modify(Dim)
                    }
                }
            }
            onValue(value)
        }
    }

    val element = column {
        addModifiers(modifiers, Gap4)
        row(modify(JustifyContentCenter, AlignItemsCenter)) {
            entries.forEach { value ->
                elements[value] = box(modify(BorderRadius2, Padding1)) {
                    heading3("${value.ordinal + 1}. ${value.name}")
                }.onClick {
                    if (value.ordinal >= (currentValue?.ordinal ?: 0)) return@onClick
                    selectElement(value)
                }
                if (value.ordinal < entries.size - 1) {
                    icon(SvgFile.ArrowRight, modify(Height3, Dim))
                }
            }
        }
        flowBlock(flow, modify(Blur), block = block)
    }

    renderScope.launch {
        flow.collect {
            selectElement(it)
        }
    }

    return element
}