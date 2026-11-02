@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.SvgFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.routeBlock
import koala.external.resizeFlow
import koala.model.NullContent
import koala.modifier.*
import kotlinx.css.pct
import kotlinx.html.js.canvas
import koala.dom.component.div
import koala.component.IconStyle
import streetlight.model.ui.StudioRoute
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.AudioSample
import streetlight.web.model.audioFilters
import koala.dom.component.switch
import koala.dom.component.popover
import koala.page.Id
import kotlinx.coroutines.flow.combine
import koala.dom.component.column
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
            column {
                studioControls()
                column(modify(Gap2Px, Zen, BorderRadius1, OverflowClip)) {
                    studioTimeline()
                    sampleStage()
                }
                calibrationSection()
            }
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
private fun ViewScope.studioControls() = row(AlignSelfCenter) {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    button(SvgFile.PlayerRecord.filled, { model.record() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Record")))
}

/** Renders the calibration's button, and the acoustic profile it measured once there is one. */
context(model: StudioEditor)
private fun ViewScope.calibrationSection() = column {
    flowBlock(model.isCalibratingState) { isCalibrating ->
        if (isCalibrating) textBlock("Calibrating…")
        else button("Calibrate", { model.calibrate() })
    }
    flowBlock(model.profileState) { profile ->
        if (profile == null) return@flowBlock
        column {
            textBlock("Mic: ${profile.micLabel}")
            textBlock("Latency: ${(profile.latencySeconds * 1000).roundToInt()} ms")
            textBlock("Noise floor: ${profile.noiseFloorDb.toTenthsText()} dB")
            textBlock("Dynamic range: ${profile.dynamicRangeDb.toTenthsText()} dB")
            textBlock("Rumble: ${profile.rumbleDb.toTenthsText()} dB")
            textBlock("Hiss: ${profile.hissDb.toTenthsText()} dB")
            textBlock("Hum: ${profile.humHz?.let { "$it Hz" } ?: "none"}")
            textBlock("Reverb (RT60): ${profile.reverbSeconds?.let { "${(it * 1000).roundToInt()} ms" } ?: "unmeasured"}")
            textBlock("Clarity (C50): ${profile.clarityDb?.let { "${it.toTenthsText()} dB" } ?: "unmeasured"}")
            textBlock("Sweep level: ${profile.responseDb.toTenthsText()} dB")
            textBlock("Peak: ${profile.peakDb.toTenthsText()} dB")
            textBlock("Browser processing: ${profile.processing.joinToString { it.label }.ifEmpty { "none" }}")
            textBlock("Sample rate: ${profile.sampleRate} Hz")
        }
    }
}

/** Renders the timeline, with a handle at the cursor that moves it by dragging or by pressing the timeline. */
context(model: StudioEditor)
private fun ViewScope.studioTimeline() {
    lateinit var timelineElement: HTMLElement
    lateinit var handleElement: HTMLElement
    div(modify(StudioStyle.Track, CardBg)) {
        flowBlock(model.endSecondsState, Padding(1)) { endSeconds ->
            textBlock(endSeconds?.toDurationText() ?: "0:00")
        }
        timelineElement = div(modify(StudioStyle.Timeline, VoidBg)) {
            handleElement = div(StudioStyle.CursorHandle)
        }
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
private fun ViewScope.sampleStage() = flowBlock(model.sampleIdsState, modify(Gap2Px)) { ids ->
    ids.forEach { id ->
        model.readSample(id)?.let { audioSample(it) }
    }
}

/**
 * Renders [sample] beside its name and switches, as its waveform at the timeline's scale, with a line at the cursor.
 */
context(model: StudioEditor)
fun ViewScope.audioSample(sample: AudioSample) {
    val filtersId = Id("sample-filters-${sample.id}")
    lateinit var canvasElement: HTMLCanvasElement
    lateinit var lineElement: HTMLElement
    div(modify(StudioStyle.Track, CardBg)) {
        column(Padding(1)) {
            textBlock(sample.name)
            switch("mute", model.isMutedState(sample.id))
            button("filters") { setPopoverTarget(filtersId, true) }
        }
        div(modify(Height(32), StudioStyle.Lane)) {
            canvasElement = this@audioSample.canvas { addModifiers(VoidBg, PrimaryFg, StudioStyle.Waveform) }
                .unsafeCast<HTMLCanvasElement>()
            lineElement = div(StudioStyle.CursorLine)
        }
    }

    fun sizeWaveform(spanSeconds: Double) =
        canvasElement.setStyle(Css.Width.of((sample.durationSeconds / spanSeconds * 100).pct))

    popover(filtersId) {
        column {
            audioFilters.forEach { filter ->
                switch(filter.label, model.isFilteredState(sample.id, filter))
            }
        }
    }

    placeAtCursor(lineElement)
    sizeWaveform(model.spanSecondsState.now)

    launchEffect("audioSample") {
        model.spanSecondsState.flow.collect { sizeWaveform(it) }
    }

    launchEffect("audioSample") {
        combine(canvasElement.resizeFlow(), model.filtersState(sample.id).flow) { _, _ -> }.collect {
            model.readPcm(sample.id)?.let { canvasElement.drawWaveform(it) }
        }
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

/** Draws [sample] across this canvas in its text color. */
private fun HTMLCanvasElement.drawWaveform(sample: PcmSample) {
    // size the drawing surface to the device's pixels
    val rect = getBoundingClientRect()
    width = (rect.width * devicePixelRatio).roundToInt()
    height = (rect.height * devicePixelRatio).roundToInt()
    val context = getContext(CanvasRenderingContext2D.ID) ?: return

    // draw a bar from the low to the high peak of each column
    val peaks = sample.readPeaks(width)
    val middle = height / 2.0
    context.fillStyle = getComputedStyle(this).getPropertyValue("color").toJsString()
    for (column in 0 until width) {
        val top = middle - peaks[column * 2 + 1] * middle
        val bottom = middle - peaks[column * 2] * middle
        context.fillRect(column.toDouble(), top, 1.0, maxOf(bottom - top, 1.0))
    }
}

/** These seconds as minutes, seconds and tenths, as `1:04.2`. */
private fun Double.toDurationText(): String {
    val tenths = (this * 10).roundToInt()
    val seconds = tenths / 10 % 60
    return "${tenths / 600}:${seconds.toString().padStart(2, '0')}.${tenths % 10}"
}

/** This value rounded to tenths, as `-62.3`. */
private fun Double.toTenthsText() = ((this * 10).roundToInt() / 10.0).toString()
