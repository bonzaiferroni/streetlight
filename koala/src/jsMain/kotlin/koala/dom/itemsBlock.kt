package koala.dom

import koala.css.*
import koala.html.ItemsBlockKey
import koala.model.mapDistinct
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.collections.plus

// dynamically render a list of items using a lambda of the individual item
fun <Item> RenderContext.itemsBlock(
    flow: Flow<List<Item>>,
    mod: ModifierSet? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    containerConfig: (DIV.() -> Unit)? = null,
    block: DOMContext.(Item) -> Unit
): HTMLDivElement {
    // modification with Magic animates the element when items change
    // base: item opacity fade on entrance/exit, item position is animated, base element height is animated
    // slide: items slide in/out from the given direction, slide left is most common
    // blur: item blur transitions on entrance/exit
    // A gap is provided between items, similar to flex gap. Inelegant solution, should be determined by unit-spacing.
    val magic = mod?.contains(Magic) ?: false
    var displayedItems: Map<Item, HTMLElement>? = null
    val gapPx = gapRems?.let { remToPx(it) }
    var resizeJob: Job? = null
    var heightNow = 0

    val parent = div {
        addModifiers(ItemsBlockKey.Class, mod)
        if (magic) {
            classes += Magic.identifier
        }
        config?.invoke(this)
    }

    fun createItem(item: Item): HTMLElement {
        val container = parent.append {
            div {
                containerConfig?.invoke(this)
                block(item)
            }
        }.first()
        return container
    }

    renderScope.launch {
        flow.collect { items ->
            displayedItems?.forEach { (item, element) ->
                if (!items.contains(item)) {

                    if (magic) {
                        renderScope.launch {
                            element.unmodify(Reveal)
                            delay(200)
                            element.remove()
                        }
                    } else {
                        element.remove()
                    }
                }
            }

            displayedItems = items.associateWith { item ->
                val isCurrentlyDisplayed = displayedItems?.contains(item) ?: false
                val element = displayedItems?.get(item) ?: createItem(item)

                if (magic && !isCurrentlyDisplayed) {
                    renderScope.launch {
                        delay(200)
                        element.modify(Reveal)
                    }
                }

                element
            }

            // the base element height is set/animated each time the items change
            resizeJob = launch {
                resizeJob?.cancel()
                var index = 0
                var height = 0

                displayedItems.forEach {
                    val container = it.value
                    container.style.top = "${height}px"
                    height += container.offsetHeight
                    if (gapPx != null && index + 1 < items.size) {
                        height += gapPx
                    }
                    index++
                }

                val isShrinking = height < heightNow
                heightNow = height

                if (magic && isShrinking) {
                    // allow animated content to exit before shrink
                    delay(200)
                }

                parent.style.height = "${height}px"
            }
        }
    }

    return parent
}

// for when you really need to know the index of the item within its context
fun <Item> RenderContext.indexedItemsBlock(
    flow: Flow<List<Item>>,
    modifiers: ModifierSet? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    containerConfig: (DIV.() -> Unit)? = null,
    block: RenderContext.(IndexedItem<Item>) -> Unit
): HTMLDivElement {
    val flow = flow.mapDistinct { it.mapIndexed { index, item -> IndexedItem(index, item) } }
    return itemsBlock(
        flow = flow,
        mod = modifiers,
        gapRems = gapRems,
        config = config,
        containerConfig = containerConfig,
        block = { block(it) }
    )
}

data class IndexedItem<T>(
    val index: Int,
    val value: T,
)

private fun remToPx(rem: Float) = window.getComputedStyle(document.documentElement!!).fontSize.dropLast(2).toDouble().let {
    (it * rem).toInt()
}