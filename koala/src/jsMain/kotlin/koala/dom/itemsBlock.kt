package koala.dom

import koala.css.*
import koala.html.ItemsBlockStyle
import koala.model.Tap
import koala.model.tapOf
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.div
import web.html.HTMLDivElement
import web.html.HTMLElement
import kotlin.collections.plus
import kotlin.time.Duration.Companion.milliseconds

fun <Item> ViewScope.itemsBlock(
    state: Tap<List<Item>>,
    mod: ModifierSet? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(Item) -> Unit
): HTMLDivElement {
    val magic = mod?.contains(Magic) ?: false
    var displayedItems: Map<Item, ViewElement>? = null
    val gapPx = gapRems?.let { remToPx(it) }
    var resizeJob: Job? = null
    // var heightNow = 0

    val parent = div {
        addModifiers(ItemsBlockStyle.Class, mod)
        if (magic) {
            classes += Magic.identifier
        }
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

    launchEffect {
        state.flow.collect { items ->
            if (items.isNotEmpty()) parent.unmodify(DisplayNone)

            displayedItems?.forEach { (item, viewElement) ->
                if (!items.contains(item)) {
                    if (magic) {
                        parentScope.launch {
                            viewElement.element.unmodify(Reveal)
                            delay(MagicStyle.Interval.milliseconds)
                            viewElement.dispose()
                        }
                    } else {
                        viewElement.dispose()
                    }
                }
            }

            displayedItems = items.associateWith { item ->
                val isCurrentlyDisplayed = displayedItems?.contains(item) ?: false
                val viewElement = displayedItems?.get(item) ?: createItem(item)

                if (magic && !isCurrentlyDisplayed) {
                    parentScope.launch {
                        delay(MagicStyle.Interval.milliseconds)
                        viewElement.element.modify(Reveal)
                    }
                }

                viewElement
            }

            // the base element height is set/animated each time the items change
            resizeJob?.cancel()
            resizeJob = launch {
                // val isShrinking = height < heightNow
                // heightNow = height
                if (magic) {
                    // allow animated content to exit before shrink
                    delay(MagicStyle.Interval.milliseconds)
                }

                var index = 0
                var height = 0

                displayedItems.forEach {
                    val viewElement = it.value
                    viewElement.element.style.top = "${height}px"
                    height += viewElement.element.offsetHeight
                    if (gapPx != null && index + 1 < items.size) {
                        height += gapPx
                    }
                    index++
                }

                if (height == 0) parent.modify(DisplayNone)
                parent.style.height = "${height}px"
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

fun <Item> ViewScope.indexedItemsBlock(
    state: Tap<List<Item>>,
    modifiers: ModifierSet? = null,
    gapRems: Float? = 0.5f,
    config: (DIV.() -> Unit)? = null,
    block: ViewScope.(IndexedItem<Item>) -> Unit
): HTMLDivElement {
    val indexedState = state.tapOf { it.mapIndexed { index, item -> IndexedItem(index, item) } }
    return itemsBlock(
        state = indexedState,
        mod = modifiers,
        gapRems = gapRems,
        config = config,
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