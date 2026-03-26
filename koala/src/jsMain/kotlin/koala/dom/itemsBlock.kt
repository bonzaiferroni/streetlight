package koala.dom

import koala.css.*
import koala.model.mapDistinct
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.collections.plus

fun <Item> RenderContext.itemsBlock(
    flow: Flow<List<Item>>,
    modifiers: ModifierSet? = null,
    magic: Boolean = false,
    gapRems: Float? = 0.5f,
    cacheRenderedElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    containerConfig: (DIV.() -> Unit)? = null,
    block: RenderContext.(Item) -> Unit
): HTMLDivElement {
    val parent = div {
        addModifiers(ElementClass.itemsBlock, modifiers)
        if (magic) {
            classes += Magic.value
        }
        config?.invoke(this)
    }

    val cachedItems = mutableMapOf<Item, RenderCache>()
    var displayedItems: Map<Item, RenderCache>? = null
    val gapPx = gapRems?.let { remToPx(it) }

    fun createItem(item: Item): RenderCache {
        val job = SupervisorJob()
        val localScope = CoroutineScope(Dispatchers.Main + job)
        val container = parent.append {
            div {
                containerConfig?.invoke(this)
            }
        }.first()
        var context: RenderContext
        container.append {
            context = DOMRenderContext(this, localScope)
            context.block(item)
        }
        return RenderCache(context, job, localScope, listOf(container))
    }

    fun recallCachedItem(item: Item): RenderCache? {
        val cachedItem = cachedItems[item] ?: return null
        parent.append(cachedItem.elements)
        return cachedItem
    }

    renderScope.launch {
        flow.collect { items ->
            displayedItems?.forEach { (item, cache) ->
                if (!items.contains(item)) {
                    if (cacheRenderedElements) {
                        cachedItems[item] = cache
                    } else {
                        cache.job.cancel()
                    }
                    if (magic) {
                        renderScope.launch {
                            cache.firstElement.unmodify(Reveal)
                            delay(200)
                            cache.firstElement.remove()
                        }
                    } else {
                        cache.firstElement.remove()
                    }
                }
            }

            displayedItems = items.associateWith { item ->
                val isCurrentlyDisplayed = displayedItems?.contains(item) ?: false
                val cache = displayedItems?.get(item) ?: recallCachedItem(item) ?: createItem(item)

                if (magic && !isCurrentlyDisplayed) {
                    cache.localScope.launch {
                        delay(200)
                        cache.firstElement.modify(Reveal)
                    }
                }

                cache
            }

            window.requestAnimationFrame {
                var index = 0
                var height = 0

                displayedItems.forEach {
                    val container = it.value.firstElement
                    container.style.top = "${height}px"
                    height += container.offsetHeight
                    console.log(container.offsetHeight)
                    if (gapPx != null && index + 1 < items.size) {
                        height += gapPx
                    }
                    index++
                }

                parent.style.height = "${height}px"
            }
        }
    }

    return parent
}

fun <Item> RenderContext.indexedItemsBlock(
    flow: Flow<List<Item>>,
    modifiers: ModifierSet? = null,
    magic: Boolean = false,
    gapRems: Float? = 0.5f,
    cacheRenderedElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    containerConfig: (DIV.() -> Unit)? = null,
    block: RenderContext.(IndexedItem<Item>) -> Unit
): HTMLDivElement {
    val flow = flow.mapDistinct { it.mapIndexed { index, item -> IndexedItem(index, item) } }
    return itemsBlock(
        flow = flow,
        modifiers = modifiers,
        magic = magic,
        gapRems = gapRems,
        cacheRenderedElements = cacheRenderedElements,
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