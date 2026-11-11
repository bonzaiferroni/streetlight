package koala.dom.layout

import koala.modifier.*
import kampfire.model.Tap
import kampfire.model.tapOf
import kotlinx.css.rem
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.dom.document
import web.html.HTMLDivElement
import web.html.HTMLElement
import koala.dom.View
import koala.dom.ViewScope
import koala.dom.appendChildView
import koala.dom.asWeb
import koala.dom.launchEffect
import koala.dom.viewTransition
import koala.layout.ItemsBlockStyle

/**
 * Renders each item of [state] as a child view in a column [gapRems] apart, keeping the views of items that stay.
 *
 * With [Transitioner] in [mod], each change runs inside a view transition: items that stay move to their places, and
 * items that arrive or leave pop in or out. The block hides itself while empty.
 */
fun <Item> ViewScope.itemsBlock(
    state: Tap<List<Item>>,
    mod: Modifier? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(Item) -> Unit
): HTMLDivElement {
    val isTransitioner = mod?.contains(Transitioner) ?: false
    var displayedItems: Map<Item, ViewElement>? = null

    val parent = div {
        addModifiers(ItemsBlockStyle.Class, mod, gapRems?.let { Css.Gap.of(it.rem) })
        config?.invoke(this)
    }.asWeb()

    fun createItem(item: Item): ViewElement {
        lateinit var element: HTMLElement
        val view = parent.appendChildView("itemsBlock", this) {
            element = div {
                block(item)
            }.asWeb()
        }
        return ViewElement(view, element)
    }

    fun display(items: List<Item>) {
        displayedItems?.forEach { (item, viewElement) ->
            if (item !in items) viewElement.dispose()
        }
        val nextItems = items.associateWith { item -> displayedItems?.get(item) ?: createItem(item) }
        // only an element out of place is moved, since moving one restarts its animations
        nextItems.values.forEachIndexed { index, viewElement ->
            val occupant = parent.children.item(index)
            if (occupant !== viewElement.element) parent.insertBefore(viewElement.element, occupant)
        }
        displayedItems = nextItems
    }

    launchEffect {
        state.flow.collect { items ->
            when {
                isTransitioner && displayedItems != null -> document.viewTransition { display(items) }
                else -> display(items)
            }
        }
    }

    return parent
}

private data class ViewElement(
    val view: View,
    val element: HTMLElement
) {
    fun dispose() {
        element.remove()
        view.dispose()
    }
}

/** [itemsBlock] with each item paired with its index. */
fun <Item> ViewScope.indexedItemsBlock(
    state: Tap<List<Item>>,
    mod: Modifier? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(IndexedItem<Item>) -> Unit
): HTMLDivElement {
    val indexedState = state.tapOf { it.mapIndexed { index, item -> IndexedItem(index, item) } }
    return itemsBlock(
        state = indexedState,
        mod = mod,
        gapRems = gapRems,
        config = config,
        block = { block(it) }
    )
}

/** An item and its index in a list. */
data class IndexedItem<T>(
    val index: Int,
    val value: T,
)
