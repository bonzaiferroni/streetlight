package koala.dom

import kampfire.model.Labeled
import koala.SvgFile
import koala.css.*
import koala.html.heading3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.enums.enumEntries

inline fun <reified State> RenderScope.stageBlock(
    flow: Flow<State>,
    crossinline onValue: (State) -> Unit,
    modifiers: ModifierSet? = null,
    crossinline isHeadingStage: (State) -> Boolean = { true },
    noinline block: RenderScope.(State) -> Unit
): HTMLDivElement where State: Enum<State>, State: Labeled {
    val entries = enumEntries<State>()
    var currentValue: State? = null
    val elements = mutableMapOf<State, HTMLElement>()

    val selectElement: (State) -> Unit = { value ->
        if (value != currentValue) {
            currentValue = value
            val currentOrdinal = currentValue.ordinal
            if (elements.containsKey(value)) {
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
            }
            onValue(value)
        }
    }

    val element = column {
        addModifiers(modifiers, Gap4)
        row(modify(JustifyContentCenter, AlignItemsCenter)) {
            var step = 1
            entries.forEach { value ->
                if (!isHeadingStage(value)) return@forEach

                elements[value] = box(modify(BorderRadius2, Padding1)) {
                    heading3("${step++}. ${value.label}")
                }.onClick {
                    if (value.ordinal >= (currentValue?.ordinal ?: 0)) return@onClick
                    selectElement(value)
                }
                if (value.ordinal < entries.size - 1) {
                    icon(SvgFile.ArrowRight, modify(Height3, Dim))
                }
            }
        }
        flowBlock(flow, modify(Magic, Blur), block = block)
    }

    parentScope.launch {
        flow.collect {
            selectElement(it)
        }
    }

    return element
}