package koala.dom.layout

import kampfire.model.MutableTap
import koala.dom.View
import koala.dom.ViewScope
import koala.dom.component.div
import koala.dom.createDiv
import koala.dom.launchEffect
import koala.dom.mountChildView
import koala.layout.OrderColumnStyle
import koala.modifier.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import js.array.ReadonlyArray
import js.objects.unsafeJso
import web.animations.Keyframe
import web.animations.awaitAnimationFrame
import web.cssom.getComputedStyle
import web.dom.Element
import web.dom.document
import web.events.addEventHandler
import web.events.addEventListener
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.keyboard.KEY_DOWN
import web.keyboard.KeyboardEvent
import web.pointer.LOST_POINTER_CAPTURE
import web.pointer.POINTER_CANCEL
import web.pointer.POINTER_DOWN
import web.pointer.POINTER_MOVE
import web.pointer.POINTER_UP
import web.pointer.PointerEvent
import web.window.window

/** A handle on an item of an [orderColumn], which moves the item within the column. */
class OrderHandle internal constructor(
    private val onStart: (PointerEvent) -> Unit,
) {
    /** Starts moving the item with the pointer pressed in [event], until the pointer is released. */
    fun startMove(event: PointerEvent) = onStart(event)

    /** Makes [element] a grip, which starts moving the item when pressed. */
    fun grip(element: HTMLElement) {
        element.modify(OrderColumnStyle.Grip)
        element.addEventListener(PointerEvent.POINTER_DOWN, { startMove(it) })
    }
}

/**
 * Renders each item of [state] in a column, built by [content] with a handle that moves it.
 *
 * Items are known by [keyOf]: an item whose key stays keeps its view, built from the item as it first came. A moving
 * item fades and takes its place among the others as the pointer passes their middles, each item sliding to its new
 * place, and scrolls the column's scroller near its edges. Releasing the pointer writes the new order to [state], and Escape puts the items back, as
 * does an item added or removed along the way.
 */
fun <T, K> ViewScope.orderColumn(
    state: MutableTap<List<T>>,
    keyOf: (T) -> K,
    mod: Modifier? = null,
    content: context(OrderHandle) ViewScope.(T) -> Unit,
): HTMLDivElement {
    val column = div(modify(mod, OrderColumnStyle.Column)) { }
    val items = mutableMapOf<K, OrderItem>()
    var order = listOf<K>()
    var move: OrderMove<K>? = null

    /** Puts the elements in [order], sliding each that moves from where it was when [isAnimated]. */
    fun place(isAnimated: Boolean = false) {
        val before = if (isAnimated) items.mapValues { it.value.element.getBoundingClientRect().top } else emptyMap()
        items.values.forEach { item -> item.element.getAnimations().forEach { it.cancel() } }
        order.forEach { key -> items[key]?.let { column.appendChild(it.element) } }
        before.forEach { (key, top) ->
            val element = items[key]?.element ?: return@forEach
            val offset = top - element.getBoundingClientRect().top
            if (offset != 0.0) element.animate(slideFrom(offset), SLIDE_MS)
        }
    }

    /**
     * Places the moving item where [clientY] falls among the middles of the others, measured from the layout so a
     * slide under way does not move them.
     */
    fun follow(move: OrderMove<K>, clientY: Double) {
        val columnTop = column.getBoundingClientRect().top
        val others = order.filter { it != move.key }
        val index = others.count { key ->
            val element = items[key]?.element ?: return@count false
            columnTop + element.offsetTop + element.offsetHeight / 2.0 < clientY
        }
        val moved = others.toMutableList().apply { add(index, move.key) }
        if (moved != order) {
            order = moved
            place(isAnimated = true)
        }
    }

    fun end(isCommitted: Boolean) {
        val ending = move ?: return
        move = null
        ending.scrollJob?.cancel()
        ending.removeEscape()
        items[ending.key]?.element?.unmodify(OrderColumnStyle.Moving)
        if (column.hasPointerCapture(ending.pointerId)) column.releasePointerCapture(ending.pointerId)
        if (!isCommitted) {
            order = ending.startOrder.filter { it in items }
            place(isAnimated = true)
        } else if (order != ending.startOrder) {
            val moved = order
            state.update { list ->
                val byKey = list.associateBy(keyOf)
                moved.mapNotNull { byKey[it] } + list.filter { keyOf(it) !in moved }
            }
        }
    }

    fun start(key: K, event: PointerEvent) {
        if (move != null || key !in items) return
        event.preventDefault()
        column.setPointerCapture(event.pointerId)
        val removeEscape = window.addEventHandler(KeyboardEvent.KEY_DOWN) { keyEvent ->
            if (keyEvent.key == "Escape") end(isCommitted = false)
        }
        move = OrderMove(key, event.pointerId, order, removeEscape, event.clientY.toDouble())
        items[key]?.element?.modify(OrderColumnStyle.Moving)
    }

    /** Scrolls the column's scroller while the pointer of [moving] stays near its edges, following as it goes. */
    fun scrollNearEdge(moving: OrderMove<K>) {
        if (moving.scrollJob?.isActive == true) return
        val scroller = column.scroller()
        moving.scrollJob = contentScope.launch {
            while (move === moving) {
                val bounds = scroller.getBoundingClientRect()
                val top = maxOf(bounds.top, 0.0)
                val bottom = minOf(bounds.bottom, window.innerHeight.toDouble())
                val step = when {
                    moving.clientY < top + EDGE_PX -> -(top + EDGE_PX - moving.clientY)
                    moving.clientY > bottom - EDGE_PX -> moving.clientY - (bottom - EDGE_PX)
                    else -> break
                } * SCROLL_RATE
                scroller.scrollTop += step
                follow(moving, moving.clientY)
                awaitAnimationFrame()
            }
        }
    }

    column.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        val moving = move?.takeIf { it.pointerId == event.pointerId } ?: return@addEventListener
        moving.clientY = event.clientY.toDouble()
        follow(moving, moving.clientY)
        scrollNearEdge(moving)
    })
    column.addEventListener(PointerEvent.POINTER_UP, { event ->
        if (move?.pointerId == event.pointerId) end(isCommitted = true)
    })
    column.addEventListener(PointerEvent.POINTER_CANCEL, { end(isCommitted = false) })
    column.addEventListener(PointerEvent.LOST_POINTER_CAPTURE, { end(isCommitted = false) })
    onDispose { end(isCommitted = false) }

    fun create(item: T): OrderItem {
        val key = keyOf(item)
        val element = document.createDiv { }
        column.appendChild(element)
        val handle = OrderHandle { event -> start(key, event) }
        val view = mountChildView("orderColumnItem", element) { content(handle, this, item) }
        return OrderItem(view, element)
    }

    launchEffect {
        state.flow.collect { list ->
            val keys = list.map(keyOf)
            // an edit to an item leaves a move alone, while an item added or removed ends it
            if (move != null) {
                if (keys.toSet() == items.keys) return@collect
                end(isCommitted = false)
            }
            (items.keys - keys.toSet()).forEach { key -> items.remove(key)?.dispose() }
            list.forEach { item -> if (keyOf(item) !in items) items[keyOf(item)] = create(item) }
            order = keys
            place()
        }
    }

    return column
}

/** The view of an item of an [orderColumn] and the element that holds it. */
private class OrderItem(
    val view: View,
    val element: HTMLElement,
) {
    fun dispose() {
        element.remove()
        view.dispose()
    }
}

/** A move under way of the item at [key], by the pointer [pointerId], from [startOrder]. */
private class OrderMove<K>(
    val key: K,
    val pointerId: Int,
    val startOrder: List<K>,
    val removeEscape: () -> Unit,
    var clientY: Double,
) {
    var scrollJob: Job? = null
}

/** The nearest ancestor of this element that scrolls, or the page. */
private fun Element.scroller(): Element {
    var element = parentElement
    while (element != null) {
        val overflow = getComputedStyle(element).overflowY
        if ((overflow == "auto" || overflow == "scroll") && element.scrollHeight > element.clientHeight) return element
        element = element.parentElement
    }
    return document.scrollingElement ?: document.documentElement
}

/** The keyframes that slide an element from [offset] pixels away back to its place. */
private fun slideFrom(offset: Double): ReadonlyArray<Keyframe> {
    val from = unsafeJso<Keyframe> { easing = "ease-out" }
    from.asDynamic().transform = "translateY(${offset}px)"
    val to = unsafeJso<Keyframe>()
    to.asDynamic().transform = "none"
    return arrayOf(from, to).unsafeCast<ReadonlyArray<Keyframe>>()
}

/** The length of a slide in milliseconds. */
private const val SLIDE_MS = 150.0

/** The distance from a scroller's edge within which a moving item scrolls it. */
private const val EDGE_PX = 48.0

/** The share of the pointer's reach into the edge that a frame scrolls. */
private const val SCROLL_RATE = 0.25
