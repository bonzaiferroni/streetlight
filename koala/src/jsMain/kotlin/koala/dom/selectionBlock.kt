package koala.dom

import koala.css.*
import koala.model.MutableTap
import koala.model.Tap
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import web.html.HTMLDivElement
import web.html.HTMLElement

fun <Item> ViewScope.selectionBlock(
    items: Tap<List<Item>>,
    selection: MutableTap<Item?>,
    modifiers: ModifierSet? = null,
    emptyText: String? = null,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(Item) -> HTMLElement
): HTMLDivElement {
    var selectedElement: HTMLElement? = null
    val elementMap = mutableMapOf<Item, HTMLElement>()

    fun selectElement(item: Item?) {
        when (item) {
            null -> {
                selectedElement?.unmodify(Selected)
                selectedElement = null
            }
            else -> {
                val element = elementMap[item] ?: return
                if (element.isModified(Selected)) {
                    element.unmodify(Selected)
                    selectedElement = null
                } else {
                    selectedElement?.unmodify(Selected)
                    element.modify(Selected)
                    selectedElement = element

                }
            }
        }
        if (item == selection.now) return
        selection.set(item)
    }

    val element = flowBlock(items, modifiers, config = config) { items ->
        elementMap.clear()
        when (items.isNotEmpty()) {
            true -> column {
                items.forEach { item ->
                    val element = block(item).onClick {
                        selectElement(item)
                    }
                    elementMap[item] = element
                }
            }
            else -> box {
                emptyText?.let {
                    textBlock(it, modify(TextSmall, PlaceSelfCenter, OpacityHigh))
                }
            }
        }

    }

    contentScope.launch {
        selection.flow.collect { item ->
            selectElement(item)
        }
    }

    return element
}