package koala.dom

import koala.css.ModifierSet
import koala.css.Outlined
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun <Item> ViewScope.selectionBlock(
    flow: Flow<List<Item>>,
    onSelect: (Item?) -> Unit,
    selectFlow: Flow<Item?>? = null,
    modifiers: ModifierSet? = null,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(Item) -> HTMLElement
): HTMLDivElement {
    var selectedItem: Item? = null
    var selectedElement: HTMLElement? = null
    val elementMap = mutableMapOf<Item, HTMLElement>()

    fun selectElement(item: Item?) {
        selectedItem = item
        when (item) {
            null -> {
                selectedElement?.unmodify(Outlined)
                selectedElement = null
            }
            else -> {
                val element = elementMap.getValue(item)
                if (element.isModified(Outlined)) {
                    onSelect(null)
                    element.unmodify(Outlined)
                    selectedElement = null
                } else {
                    selectedElement?.unmodify(Outlined)
                    element.modify(Outlined)
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

    contentScope.launch {
        selectFlow?.collect { item ->
            if (item == selectedItem) return@collect
            selectElement(item)
        }
    }

    return element
}