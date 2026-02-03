package koala.dom

import koala.css.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
        applyModifiers(ElementClass.flowBlock, modifiers)
        if (animate) {
            classes += Animate.value
        }
        config?.invoke(this)
    }

    val cachedItems = mutableMapOf<Item, ItemCache>()
    var displayedItems: Map<Item, ItemCache>? = null

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
        return ItemCache(job, container)
    }

    renderScope.launch {
        flow.collect { items ->

            displayedItems?.forEach { (item, cache) ->
                if (!items.contains(item)) {
                    cache.container.remove()
                    if (cacheRenderedElements) {
                        cachedItems[item] = cache
                    }
                }
            }

            var height = 0
            displayedItems = items.associateWith { item ->
                val cachedItem = displayedItems?.get(item) ?: cachedItems[item] ?: createItem(item)
                val container = cachedItem.container
                container.style.top = "${height}px"
                height += container.offsetHeight
                cachedItem
            }
        }
    }
}

private data class ItemCache(
    val job: Job,
    val container: HTMLElement
)