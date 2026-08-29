package koala.dom

import koala.css.*
import koala.model.LazyColumnStyle
import koala.model.MutableTap
import koala.model.storeOf
import kotlinx.css.px
import kotlinx.html.js.div
import web.animations.requestAnimationFrame
import web.events.AddEventListenerOptions
import web.events.Event
import web.events.SCROLL
import web.events.addEventListener
import web.html.HTMLDivElement
import web.html.HTMLElement
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

fun <T> ViewScope.lazyColumn(
    list: LazyList<T>,
    mod: ModifierSet? = null,
    state: MutableTap<LazyColumnState> = storeOf(LazyColumnState()),
    content: AppendScope.(T) -> Unit
): HTMLDivElement {
    lateinit var container: HTMLElement
    val scroller = div(modify(mod, LazyColumnStyle.Scroller)) {
        container = div(modify(LazyColumnStyle.Container)) { }
    }

    var appendedAt: Instant? = null

    onAppend {
        appendedAt = Clock.System.now()
        val elementHeight = scroller.offsetHeight
        var contentHeight = 0
        var appendedCount = 0
        list.items.forEach { item ->
            if (contentHeight > elementHeight) return@forEach
            appendedCount++

            lateinit var itemElement: HTMLElement
            container.append {
                itemElement = div {
                    content(item)
                }.asWeb()
            }
            contentHeight += itemElement.offsetHeight
        }
        val itemHeight = (contentHeight / appendedCount.toFloat()).roundToInt()
        container.setStyle(Property.Height.to((itemHeight * list.items.size).px))
    }

    requestAnimationFrame {
        println("frame")
        appendedAt?.let {
            println("${(Clock.System.now() - it).inWholeMilliseconds}ms")
        }
    }

    val observer = IntersectionObserver({ entries, _ ->
        if (entries.any { it.isIntersecting }) {
            println("intersect")
            appendedAt?.let {
                println("${(Clock.System.now() - it).inWholeMilliseconds}ms")
            }
        }
    }, IntersectionObserverInit(
        root = scroller,
        rootMargin = "400px"
    ))

    observer.observe(container)

    scroller.addEventListener(Event.SCROLL, {
        val remaining = scroller.scrollHeight - scroller.scrollTop - scroller.clientHeight
        println("scrolled")
        if (remaining < 400) {
            println("build more")
        }
    }, AddEventListenerOptions(passive = true))

    return scroller
}

class LazyList<T>(initialItems: List<T>) {
    private val state = storeOf(initialItems.size)

    val items: List<T>
        field = initialItems.toMutableList()
}

data class LazyListState(
    val size: Int,
)

data class LazyColumnState(
    val scrollIndex: Int = 0,
)