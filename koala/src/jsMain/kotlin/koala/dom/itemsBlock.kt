package koala.dom

import koala.css.*
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
import kotlin.collections.plus

fun <Item> RenderContext.itemsBlock(
    flow: Flow<List<Item>>,
    modifiers: ModifierSet? = null,
    animate: Boolean = false,
    gapRems: Float? = 0.5f,
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
            context = RenderContext(this, localScope)
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
            console.log("collected")

            displayedItems?.forEach { (item, cache) ->
                if (!items.contains(item)) {
                    if (cacheRenderedElements) {
                        cachedItems[item] = cache
                    } else {
                        cache.job.cancel()
                    }
                    if (animate) {
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

            var height = 0
            var index = 0
            displayedItems = items.associateWith { item ->
                val isCurrentlyDisplayed = displayedItems?.contains(item) ?: false
                val cache = displayedItems?.get(item) ?: recallCachedItem(item) ?: createItem(item)
                val container = cache.firstElement
                container.style.top = "${height}px"
                height += container.scrollHeight
                if (gapPx != null && index + 1 < items.size) {
                    height += gapPx
                }

                if (animate && !isCurrentlyDisplayed) {
                    cache.localScope.launch {
                        delay(200)
                        cache.firstElement.modify(Reveal)
                    }
                }

                index++
                cache
            }

            parent.style.height = "${height}px"
        }
    }
}

private fun remToPx(rem: Float) = window.getComputedStyle(document.documentElement!!).fontSize.dropLast(2).toDouble().let {
    (it * rem).toInt()
}