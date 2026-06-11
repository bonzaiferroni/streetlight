package koala.dom

import koala.css.ModifierSet
import koala.css.Selected
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun <Item> DOMRender.selectionBlock(
    flow: Flow<List<Item>>,
    onSelect: (Item?) -> Unit,
    selectFlow: Flow<Item?>? = null,
    modifiers: ModifierSet? = null,
    config: (DIV.() -> Unit)? = null,
    block: DOMRender.(Item) -> HTMLElement
): HTMLDivElement {
    var selectedItem: Item? = null
    var selectedElement: HTMLElement? = null
    val elementMap = mutableMapOf<Item, HTMLElement>()

    fun selectElement(item: Item?) {
        selectedItem = item
        when (item) {
            null -> {
                selectedElement?.unmodify(Selected)
                selectedElement = null
            }
            else -> {
                val element = elementMap.getValue(item)
                if (element.isModified(Selected)) {
                    onSelect(null)
                    element.unmodify(Selected)
                    selectedElement = null
                } else {
                    selectedElement?.unmodify(Selected)
                    element.modify(Selected)
                    selectedElement = element
                    onSelect(item)
                }
            }
        }
    }

    val element = flowBlock(flow, modifiers, config = config) { items ->
        elementMap.clear()
        column {
            items.forEach { item ->
                val element = block(item).onClick {
                    selectElement(item)
                }
                elementMap[item] = element
            }
        }
    }

    renderScope.launch {
        selectFlow?.collect { item ->
            if (item == selectedItem) return@collect
            selectElement(item)
        }
    }

    return element
}