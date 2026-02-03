package koala.dom

import koala.css.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import kotlin.collections.plus

fun <Item> RenderContext.itemsBlock(
    flow: Flow<List<Item>>,
    modifiers: ModifierSet? = null,
    animate: Boolean = false,
    cacheRenderedElements: Boolean = false,
    config: (DIV.() -> Unit)? = null,
    containerConfig: (DIV.() -> Unit)? = null,
    block: RenderContext.(Item) -> Unit
) {
    val parent = div {
        applyModifiers(ElementClass.itemsBlock, modifiers)
        if (animate) {
            classes += Animate.value
        }
        config?.invoke(this)
    }

    val cachedItems = mutableMapOf<Item, ItemCache>()
    var displayedItems: Map<Item, ItemCache>? = null
    val gapPx = remToPx(0.5)

    fun createItem(item: Item): ItemCache {
        val job = SupervisorJob()
        val localScope = CoroutineScope(Dispatchers.Main + job)
        val container = parent.append {
            div {
                containerConfig?.invoke(this)
            }
        }.first()
        container.append {
            RenderContext(this, localScope).block(item)
        }
        return ItemCache(job, localScope, container)
    }

    fun recallCachedItem(item: Item): ItemCache? {
        val cachedItem = cachedItems[item] ?: return null
        parent.append(cachedItem.container)
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
                    if (animate) {
                        renderScope.launch {
                            cache.container.unmodify(Reveal)
                            delay(200)
                            cache.container.remove()
                        }
                    } else {
                        cache.container.remove()
                    }
                }
            }

            var height = 0
            var index = 0
            displayedItems = items.associateWith { item ->
                val isCurrentlyDisplayed = displayedItems?.contains(item) ?: false
                val cache = displayedItems?.get(item) ?: recallCachedItem(item) ?: createItem(item)
                val container = cache.container
                container.style.top = "${height}px"
                height += container.offsetHeight
                if (index + 1 < items.size) {
                    height += gapPx
                }

                if (animate && !isCurrentlyDisplayed) {
                    console.log("ey")
                    cache.localScope.launch {
                        delay(200)
                        cache.container.modify(Reveal)
                    }
                }

                index++
                cache
            }

            parent.style.height = "${height}px"
        }
    }
}

private fun remToPx(rem: Double) = window.getComputedStyle(document.documentElement!!).fontSize.dropLast(2).toDouble().let {
    (it * rem).toInt()
}

private data class ItemCache(
    val job: Job,
    val localScope: CoroutineScope,
    val container: HTMLElement
)