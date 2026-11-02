@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.SvgFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.box
import koala.dom.component.button
import koala.dom.component.row
import koala.dom.launchEffect
import koala.dom.layout.flowBlock
import koala.dom.routeBlock
import koala.external.resizeFlow
import koala.model.NullContent
import koala.modifier.*
import kotlinx.coroutines.flow.combine
import kotlinx.css.pct
import kotlinx.html.js.canvas
import koala.dom.component.div
import koala.component.IconStyle
import streetlight.model.ui.StudioRoute
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.PcmSample
import streetlight.web.model.StudioEditor
import streetlight.web.model.getStudioEditor
import streetlight.web.utility.setTitle
import web.canvas.CanvasRenderingContext2D
import web.canvas.ID
import web.cssom.getComputedStyle
import web.device.devicePixelRatio
import web.dom.document
import web.events.addEventListener
import web.html.HTMLCanvasElement
import web.html.HTMLElement
import web.pointer.POINTER_DOWN
import web.pointer.POINTER_MOVE
import web.pointer.PointerEvent
import kotlin.math.roundToInt

/** Renders the studio, where samples are recorded and played together from a cursor on a timeline. */
fun ViewScope.viewStudio() {
    val model = app.getStudioEditor(contentScope)
    context(model) {
        mainBody("") {
            studioControls()
            studioTimeline()
            sampleList()
        }
    }

    document.setTitle(StudioRoute)
}

fun RouteScope.viewStudioRoute() {
    routeBlock<StudioRoute, NullContent> {
        viewStudio()
    }
}

context(model: StudioEditor)
private fun ViewScope.studioControls() = row {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    button(SvgFile.PlayerRecord.filled, { model.record() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Record")))
}

/** Renders the timeline, with a handle at the cursor that moves it by dragging or by pressing the timeline. */
context(model: StudioEditor)
private fun ViewScope.studioTimeline() {
    lateinit var handleElement: HTMLElement
    val timelineElement = box(modify(VoidBg, Height(3), StudioStyle.Timeline)) {
        handleElement = box(StudioStyle.CursorHandle)
    }

    fun moveCursor(event: PointerEvent) {
        val rect = timelineElement.getBoundingClientRect()
        if (rect.width == 0.0) return
        model.moveCursor((event.clientX - rect.left) / rect.width)
    }

    timelineElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        timelineElement.setPointerCapture(event.pointerId)
        moveCursor(event)
    })

    timelineElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (timelineElement.hasPointerCapture(event.pointerId)) moveCursor(event)
    })

    placeAtCursor(handleElement)
}

context(model: StudioEditor)
private fun ViewScope.sampleList() = flowBlock(model.sampleIdsState) { ids ->
    for (id in ids) {
        audioSample(id)
    }
}

/** Renders the sample of [id] as its waveform across the timeline's span, with a line at the cursor. */
context(model: StudioEditor)
fun ViewScope.audioSample(id: Int) {
    val sample = model.readSample(id) ?: return

    lateinit var canvasElement: HTMLCanvasElement
    lateinit var lineElement: HTMLElement
    div(modify(VoidBg, Height(32), StudioStyle.Sample)) {
        canvasElement = this@audioSample.canvas { addModifiers(PrimaryFg, StudioStyle.Waveform) }
            .unsafeCast<HTMLCanvasElement>()
        lineElement = div(StudioStyle.CursorLine)
    }

    placeAtCursor(lineElement)

    launchEffect("audioSample") {
        combine(canvasElement.resizeFlow(), model.spanSecondsState.flow) { _, spanSeconds -> spanSeconds }
            .collect { spanSeconds -> canvasElement.drawWaveform(sample, spanSeconds) }
    }
}

/** Keeps [element] at the cursor's position along its container. */
context(model: StudioEditor)
private fun ViewScope.placeAtCursor(element: HTMLElement) {
    fun place(ratio: Double) = element.setStyle(Css.Left.of((ratio * 100).pct))

    place(model.cursorRatioState.now)

    launchEffect("placeAtCursor") {
        model.cursorRatioState.flow.collect { place(it) }
    }
}

/** Draws [sample] in this canvas's text color, scaled so the canvas's width spans [spanSeconds]. */
private fun HTMLCanvasElement.drawWaveform(sample: PcmSample, spanSeconds: Double) {
    // size the drawing surface to the device's pixels
    val rect = getBoundingClientRect()
    width = (rect.width * devicePixelRatio).roundToInt()
    height = (rect.height * devicePixelRatio).roundToInt()
    val context = getContext(CanvasRenderingContext2D.ID) ?: return

    // draw a bar from the low to the high peak of each column
    val columnCount = (width * sample.durationSeconds / spanSeconds).roundToInt().coerceAtMost(width)
    val peaks = sample.readPeaks(columnCount)
    val middle = height / 2.0
    context.fillStyle = getComputedStyle(this).getPropertyValue("color").toJsString()
    for (column in 0 until columnCount) {
        val top = middle - peaks[column * 2 + 1] * middle
        val bottom = middle - peaks[column * 2] * middle
        context.fillRect(column.toDouble(), top, 1.0, maxOf(bottom - top, 1.0))
    }
}
