package koala.dom

import kampfire.model.ListChange
import kampfire.model.LiveList
import kampfire.model.MutableTap
import kampfire.model.ScrollState
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.model.LazyColumnStyle
import kotlinx.css.LinearDimension
import kotlinx.css.px
import web.dom.document
import web.events.Event
import web.events.SCROLL
import web.events.addEventListener
import web.html.HTMLDivElement
import web.html.HTMLElement
import kotlin.math.abs

fun <T, K> ViewScope.lazyColumn(
    list: LiveList<T, K>,
    mod: ModifierSet? = null,
    scrollState: MutableTap<ScrollState?>? = null,
    expectedHeight: LinearDimension = 60.px,
    content: ViewScope.(T) -> Unit
): HTMLDivElement {
    val views = mutableListOf<View>()

    val container = div(modify(mod, LazyColumnStyle.Container)) { }

    fun HTMLElement.mountView(item: T) = mountChildView("lazyColumnItem", this) {
        content(item)
    }

    fun insert(index: Int, item: T) {
        val currentElement = views.getOrNull(index)?.mount
        val itemElement = document.createDiv {
            setAttribute(Attribute.ContainIntrinsicSize, "auto $expectedHeight")
            setAttribute(Attribute.ContentVisibility, "auto")
        }
        val view = itemElement.mountView(item)
        views.add(index, view)
        container.insertBefore(itemElement, currentElement)
    }

    fun insert(index: Int, items: List<T>) {
        items.forEachIndexed { itemIndex, item ->
            insert(index + itemIndex, item)
        }
    }

    fun replace(index: Int, item: T) {
        val currentView = views[index]
        currentView.dispose()
        currentView.mount.mountView(item)
    }

    fun remove(index: Int) {
        val currentView = views[index]
        currentView.dispose()
        currentView.mount.remove()
        views.removeAt(index)
    }

    fun remove(index: Int, count: Int) {
        repeat(count) { remove(index) }
    }

    fun clear() {
        views.forEach {
            it.dispose()
        }
        views.clear()
        container.clear()
    }

    launchEffect {
        list.changeFlow.collect { change ->
            when (change) {
                ListChange.Clear -> clear()
                is ListChange.Insert<T> -> insert(change.index, change.items)
                is ListChange.Remove -> remove(change.index, change.count)
                is ListChange.Replace<T> -> replace(change.index, change.item)
            }
        }
    }

    scrollState?.let { state ->
        container.addEventListener(Event.SCROLL, {
            val offset = abs(container.scrollTop)
            val atStart = offset <= 0
            val atEnd = container.scrollHeight - offset - container.clientHeight <= 10
            state.set { ScrollState(atStart, atEnd) }
        })
    }

    return container
}

// state: MutableTap<LazyColumnState> = storeOf(LazyColumnState()),
data class LazyColumnState(
    val scrollIndex: Int = 0,
)