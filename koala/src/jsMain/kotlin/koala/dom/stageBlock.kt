package koala.dom

import kampfire.model.Labeled
import koala.SvgFile
import koala.css.*
import koala.html.heading3
import kampfire.model.MutableTap
import kotlinx.coroutines.launch
import web.html.HTMLDivElement
import web.html.HTMLElement
import kotlin.enums.enumEntries

inline fun <reified State> ViewScope.stageBlock(
    stage: MutableTap<State>,
    mod: ModifierSet? = null,
    crossinline isHeadingStage: (State) -> Boolean = { true },
    noinline block: ViewScope.(State) -> Unit
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
            stage.set(value)
        }
    }

    val element = column {
        addModifiers(mod, Gap4)
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
        flowBlock(stage, modify(Magic, Blur), block = block)
    }

    contentScope.launch {
        stage.flow.collect {
            selectElement(it)
        }
    }

    return element
}