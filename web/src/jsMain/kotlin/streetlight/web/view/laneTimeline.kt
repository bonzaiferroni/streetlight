@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.launchEffect
import koala.dom.layout.flowBlock
import koala.modifier.*
import kotlinx.coroutines.flow.combine
import kotlinx.css.Visibility
import kotlinx.css.pct
import streetlight.web.model.LaneEditor
import web.events.addEventListener
import web.html.HTMLElement
import web.pointer.POINTER_DOWN
import web.pointer.POINTER_MOVE
import web.pointer.POINTER_UP
import web.pointer.PointerEvent
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Renders the timeline, which pulls double duty: the region places the window the lanes show within the whole
 * composition, while the handle places the cursor within the window, in line with the lanes. Dragging the region
 * moves the window, dragging its right edge changes the window's length, and pressing or dragging anywhere else moves
 * the cursor. The timeline is laid out by [rowMod], the
 * layout of the lanes below it.
 */
context(model: LaneEditor)
fun ViewScope.laneTimeline(rowMod: Modifier = StudioStyle.Track) {
    lateinit var timelineElement: HTMLElement
    lateinit var regionElement: HTMLElement
    lateinit var edgeElement: HTMLElement
    lateinit var handleElement: HTMLElement
    div(modify(rowMod, CardBg)) {
        flowBlock(model.endSecondsState, Padding(1)) { endSeconds ->
            textBlock(endSeconds?.toDurationText() ?: "0:00")
        }
        timelineElement = div(modify(StudioStyle.Timeline, VoidBg)) {
            regionElement = div(StudioStyle.WindowRegion) {
                edgeElement = div(StudioStyle.WindowEdge)
            }
            handleElement = div(StudioStyle.CursorHandle)
        }
    }

    fun readRatio(event: PointerEvent): Double? {
        val rect = timelineElement.getBoundingClientRect()
        if (rect.width == 0.0) return null
        return (event.clientX - rect.left) / rect.width
    }

    // the cursor reads a press within the window, the region within the whole composition
    fun moveCursor(event: PointerEvent) =
        readRatio(event)?.let { model.moveCursor(model.windowState.now.toSeconds(it)) }
    fun readTotalSeconds(event: PointerEvent) = readRatio(event)?.let { it * model.totalSecondsState.now }

    // press or drag the timeline and the handle to move the cursor
    listOf(timelineElement, handleElement).forEach { element ->
        element.addEventListener(PointerEvent.POINTER_DOWN, { event ->
            event.stopPropagation()
            element.setPointerCapture(event.pointerId)
            moveCursor(event)
        })
        element.addEventListener(PointerEvent.POINTER_MOVE, { event ->
            if (element.hasPointerCapture(event.pointerId)) moveCursor(event)
        })
    }

    // drag the region to move the window, keeping the point it was grabbed by under the pointer, or press it to
    // move the cursor
    var grabX = 0
    var grabOffset = 0.0
    var isDragging = false
    regionElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        event.stopPropagation()
        regionElement.setPointerCapture(event.pointerId)
        grabX = event.clientX
        grabOffset = (readTotalSeconds(event) ?: 0.0) - model.windowState.now.startSeconds
        isDragging = false
    })
    regionElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (!regionElement.hasPointerCapture(event.pointerId)) return@addEventListener
        if (abs(event.clientX - grabX) > DRAG_THRESHOLD_PX) isDragging = true
        if (isDragging) readTotalSeconds(event)?.let { model.moveWindow(it - grabOffset) }
    })
    regionElement.addEventListener(PointerEvent.POINTER_UP, { event ->
        if (!isDragging) moveCursor(event)
    })

    // drag the region's right edge to change the window's length, which the region must not take for a press
    edgeElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        event.stopPropagation()
        edgeElement.setPointerCapture(event.pointerId)
    })
    edgeElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (!edgeElement.hasPointerCapture(event.pointerId)) return@addEventListener
        readTotalSeconds(event)?.let { model.resizeWindow(it - model.windowState.now.startSeconds) }
    })
    edgeElement.addEventListener(PointerEvent.POINTER_UP, { event -> event.stopPropagation() })

    launchEffect("laneTimeline") {
        combine(model.windowState.flow, model.totalSecondsState.flow) { window, total -> window to total }
            .collect { (window, total) ->
                regionElement.setStyle(Css.Left.of((window.startSeconds / total * 100).pct))
                regionElement.setStyle(Css.Width.of((window.spanSeconds / total * 100).pct))
            }
    }

    // place the handle in line with the lanes, hidden while the cursor is outside the window
    launchEffect("laneTimeline") {
        readCursorRatioFlow().collect { ratio ->
            handleElement.setStyle(Css.Left.of((ratio * 100).pct))
            handleElement.setStyle(Css.Visibility.of(if (ratio in 0.0..1.0) Visibility.visible else Visibility.hidden))
        }
    }
}

/** These seconds as minutes, seconds and tenths, as `1:04.2`. */
private fun Double.toDurationText(): String {
    val tenths = (this * 10).roundToInt()
    val seconds = tenths / 10 % 60
    return "${tenths / 600}:${seconds.toString().padStart(2, '0')}.${tenths % 10}"
}

// the distance a press on the window region travels before it becomes a drag
private const val DRAG_THRESHOLD_PX = 3
