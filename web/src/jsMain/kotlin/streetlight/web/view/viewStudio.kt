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
import web.pointer.POINTER_UP
import web.pointer.POINTER_CANCEL
import web.pointer.PointerEvent
import kotlin.math.abs
import kotlinx.css.Visibility
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import koala.dom.launchEffect
import streetlight.web.model.BLOCK_FRAMES
import streetlight.web.model.TimeWindow
import web.animations.awaitAnimationFrame

/** Renders the studio, where samples are recorded and played together from a cursor on a timeline. */
fun ViewScope.viewStudio() {
    val model = app.getStudioEditor(contentScope)
    context(model) {
        mainBody("") {
            column {
                studioControls()
                column(modify(Gap2Px, Zen, BorderRadius1, OverflowClip)) {
                    studioTimeline()
                    div(modify(FlexColumn, Gap2Px, StudioStyle.Stage)) {
                        sampleStage()
                        liveStage()
                        cursorOverlay()
                    }
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
        else button("Calibrate", { model.calibrate() }, Zen)
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

/**
 * Renders the timeline, which pulls double duty: the region places the window the lanes show within the whole
 * composition, while the handle places the cursor within the window, in line with the lanes. Dragging the region
 * moves the window, and pressing or dragging anywhere else moves the cursor.
 */
context(model: StudioEditor)
private fun ViewScope.studioTimeline() {
    lateinit var timelineElement: HTMLElement
    lateinit var regionElement: HTMLElement
    lateinit var handleElement: HTMLElement
    div(modify(StudioStyle.Track, CardBg)) {
        flowBlock(model.endSecondsState, Padding(1)) { endSeconds ->
            textBlock(endSeconds?.toDurationText() ?: "0:00")
        }
        timelineElement = div(modify(StudioStyle.Timeline, VoidBg)) {
            regionElement = div(StudioStyle.WindowRegion)
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

    launchEffect("studioTimeline") {
        combine(model.windowState.flow, model.totalSecondsState.flow) { window, total -> window to total }
            .collect { (window, total) ->
                regionElement.setStyle(Css.Left.of((window.startSeconds / total * 100).pct))
                regionElement.setStyle(Css.Width.of((window.spanSeconds / total * 100).pct))
            }
    }

    // place the handle in line with the lanes, hidden while the cursor is outside the window
    launchEffect("studioTimeline") {
        readCursorRatioFlow().collect { ratio ->
            handleElement.setStyle(Css.Left.of((ratio * 100).pct))
            handleElement.setStyle(Css.Visibility.of(if (ratio in 0.0..1.0) Visibility.visible else Visibility.hidden))
        }
    }
}

context(model: StudioEditor)
private fun ViewScope.sampleStage() = flowBlock(model.sampleIdsState, modify(FlexColumn, Gap2Px)) { ids ->
    ids.forEach { id ->
        model.readSample(id)?.let { audioSample(it) }
    }
}

/** Renders the cursor as one line across every lane, in line with the cursor's place in the window. */
context(model: StudioEditor)
private fun ViewScope.cursorOverlay() {
    lateinit var lineElement: HTMLElement
    div(StudioStyle.CursorOverlay) {
        lineElement = div(StudioStyle.CursorLine)
    }
    placeAt(lineElement, readCursorRatioFlow())
}

/** Renders the lane of the capture in progress while one records, apart from a calibration's. */
context(model: StudioEditor)
private fun ViewScope.liveStage() = flowBlock(model.isRecordingState) { isRecording ->
    if (isRecording && !model.isCalibratingState.now) liveLane()
}

/** Renders [sample] beside its name and switches, as its waveform within the window. */
context(model: StudioEditor)
fun ViewScope.audioSample(sample: AudioSample) {
    val filtersId = Id("sample-filters-${sample.id}")
    lateinit var laneElement: HTMLElement
    lateinit var extentElement: HTMLElement
    lateinit var canvasElement: HTMLCanvasElement
    div(modify(StudioStyle.Track, CardBg)) {
        column(Padding(1)) {
            textBlock(sample.name)
            switch("mute", model.isMutedState(sample.id))
            button("filters", mod = Zen) { setPopoverTarget(filtersId, true) }
        }
        laneElement = div(modify(Height(32), StudioStyle.Lane)) {
            extentElement = div(modify(VoidBg, StudioStyle.Extent))
            canvasElement = this@audioSample.canvas { addModifiers(PrimaryFg, StudioStyle.Waveform) }
                .unsafeCast<HTMLCanvasElement>()
        }
    }

    popover(filtersId) {
        column {
            audioFilters.forEach { filter ->
                switch(filter.label, model.isFilteredState(sample.id, filter))
            }
        }
    }

    wireSampleDrag(laneElement, sample.id)

    launchEffect("audioSample") {
        combine(
            canvasElement.resizeFlow(),
            model.windowState.flow,
            model.filtersState(sample.id).flow,
            model.startSecondsState(sample.id).flow,
        ) { _, window, _, startSeconds -> window to startSeconds }.collect { (window, startSeconds) ->
            extentElement.placeExtent(window, startSeconds, sample.durationSeconds)
            model.readPcm(sample.id)?.let { canvasElement.drawWaveform(it, startSeconds, window) }
        }
    }
}

/**
 * Moves the sample of [id] along the timeline as [laneElement] is dragged sideways. A drag begins only once the
 * pointer travels clearly sideways, so a vertical swipe still scrolls the page.
 */
context(model: StudioEditor)
private fun wireSampleDrag(laneElement: HTMLElement, id: Int) {
    var pointerId: Int? = null
    var grabX = 0
    var grabY = 0
    var grabStartSeconds = 0.0
    var isDragging = false

    laneElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        pointerId = event.pointerId
        grabX = event.clientX
        grabY = event.clientY
        grabStartSeconds = model.readSample(id)?.startSeconds ?: return@addEventListener
        isDragging = false
    })

    laneElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (event.pointerId != pointerId) return@addEventListener
        val dx = event.clientX - grabX
        val dy = event.clientY - grabY

        // begin on a clear sideways move, or let the gesture go when it turns out vertical
        if (!isDragging) {
            if (abs(dy) > SAMPLE_DRAG_THRESHOLD_PX && abs(dy) >= abs(dx)) pointerId = null
            if (abs(dx) <= SAMPLE_DRAG_THRESHOLD_PX || abs(dx) < abs(dy) * 2) return@addEventListener
            isDragging = true
            laneElement.setPointerCapture(event.pointerId)
        }

        val width = laneElement.getBoundingClientRect().width
        if (width == 0.0) return@addEventListener
        model.moveSample(id, grabStartSeconds + dx / width * model.windowState.now.spanSeconds)
    })

    listOf(PointerEvent.POINTER_UP, PointerEvent.POINTER_CANCEL).forEach { type ->
        laneElement.addEventListener(type, { event ->
            if (event.pointerId == pointerId) pointerId = null
        })
    }
}

/** Renders the capture in progress, drawing each frame only the columns its newest blocks reach. */
context(model: StudioEditor)
private fun ViewScope.liveLane() {
    lateinit var extentElement: HTMLElement
    lateinit var canvasElement: HTMLCanvasElement
    div(modify(StudioStyle.Track, CardBg)) {
        textBlock("Recording", Padding(1))
        div(modify(Height(32), StudioStyle.Lane)) {
            extentElement = div(modify(VoidBg, StudioStyle.Extent))
            canvasElement = this@liveLane.canvas { addModifiers(PrimaryFg, StudioStyle.Waveform) }
                .unsafeCast<HTMLCanvasElement>()
        }
    }

    launchEffect("liveLane") {
        var drawnWindow: TimeWindow? = null
        var nextColumn = 0
        while (true) {
            awaitAnimationFrame()
            val peaks = model.readLivePeaks() ?: break
            val startSeconds = model.readLiveStartSeconds()
            val window = model.windowState.now
            val context = canvasElement.getContext(CanvasRenderingContext2D.ID) ?: break

            // start over when the window moves or the canvas resizes
            val pixelWidth = (canvasElement.clientWidth * devicePixelRatio).roundToInt()
            if (window != drawnWindow || pixelWidth != canvasElement.width) {
                canvasElement.sizeToPixels()
                context.fillStyle = canvasElement.readColor()
                drawnWindow = window
                nextColumn = max(ceil(window.toRatio(startSeconds) * pixelWidth).toInt(), 0)
            }

            // draw the columns the new blocks reach
            extentElement.placeExtent(window, startSeconds, peaks.durationSeconds)
            val endSeconds = startSeconds + peaks.durationSeconds
            val endColumn = min(floor(window.toRatio(endSeconds) * pixelWidth).toInt(), pixelWidth)
            val blocksPerColumn = window.spanSeconds / pixelWidth * peaks.sampleRate / BLOCK_FRAMES
            val firstBlock = (window.startSeconds - startSeconds) * peaks.sampleRate / BLOCK_FRAMES
            val middle = canvasElement.height / 2.0
            for (column in nextColumn until endColumn) {
                val startBlock = (firstBlock + column * blocksPerColumn).toInt()
                val endBlock = (firstBlock + (column + 1) * blocksPerColumn).toInt()
                var low = 0f
                var high = 0f
                for (block in startBlock until min(max(endBlock, startBlock + 1), peaks.blockCount)) {
                    low = min(low, peaks.readLow(block))
                    high = max(high, peaks.readHigh(block))
                }
                context.drawPeak(column, low, high, middle)
            }
            nextColumn = max(nextColumn, endColumn)
        }
    }
}

/** Keeps [element] at each ratio of [ratioFlow] along its container. */
private fun ViewScope.placeAt(element: HTMLElement, ratioFlow: Flow<Double>) {
    launchEffect("placeAt") {
        ratioFlow.collect { element.setStyle(Css.Left.of((it * 100).pct)) }
    }
}

/** The cursor's position across the window, as it moves. */
context(model: StudioEditor)
private fun readCursorRatioFlow() =
    combine(model.cursorSecondsState.flow, model.windowState.flow) { cursor, window -> window.toRatio(cursor) }

/** Places this element over the part of [window] that a sample [durationSeconds] long from [startSeconds] covers. */
private fun HTMLElement.placeExtent(window: TimeWindow, startSeconds: Double, durationSeconds: Double) {
    val start = window.toRatio(startSeconds).coerceIn(0.0, 1.0)
    val end = window.toRatio(startSeconds + durationSeconds).coerceIn(0.0, 1.0)
    setStyle(Css.Left.of((start * 100).pct))
    setStyle(Css.Width.of((max(end - start, 0.0) * 100).pct))
}

/** Draws the part of [sample], beginning at [startSeconds], within [window] across this canvas in its text color. */
private fun HTMLCanvasElement.drawWaveform(sample: PcmSample, startSeconds: Double, window: TimeWindow) {
    sizeToPixels()
    val context = getContext(CanvasRenderingContext2D.ID) ?: return

    // find the columns the sample covers, and the frames beneath them
    val firstColumn = max(ceil(window.toRatio(startSeconds) * width).toInt(), 0)
    val lastColumn = min(floor(window.toRatio(startSeconds + sample.durationSeconds) * width).toInt(), width)
    if (lastColumn <= firstColumn) return
    val secondsPerColumn = window.spanSeconds / width
    val offsetSeconds = window.startSeconds - startSeconds
    val startFrame = ((offsetSeconds + firstColumn * secondsPerColumn) * sample.sampleRate).toInt()
    val endFrame = ((offsetSeconds + lastColumn * secondsPerColumn) * sample.sampleRate).toInt()

    val peaks = sample.readPeaks(startFrame, min(endFrame, sample.frameCount), lastColumn - firstColumn)
    val middle = height / 2.0
    context.fillStyle = readColor()
    for (column in 0 until lastColumn - firstColumn) {
        context.drawPeak(firstColumn + column, peaks[column * 2], peaks[column * 2 + 1], middle)
    }
}

// size the drawing surface to the device's pixels, which also clears it
private fun HTMLCanvasElement.sizeToPixels() {
    val rect = getBoundingClientRect()
    width = (rect.width * devicePixelRatio).roundToInt()
    height = (rect.height * devicePixelRatio).roundToInt()
}

private fun HTMLCanvasElement.readColor() = getComputedStyle(this).getPropertyValue("color").toJsString()

// a bar from the low to the high peak of one column
private fun CanvasRenderingContext2D.drawPeak(column: Int, low: Float, high: Float, middle: Double) {
    val top = middle - high * middle
    val bottom = middle - low * middle
    fillRect(column.toDouble(), top, 1.0, max(bottom - top, 1.0))
}

/** These seconds as minutes, seconds and tenths, as `1:04.2`. */
private fun Double.toDurationText(): String {
    val tenths = (this * 10).roundToInt()
    val seconds = tenths / 10 % 60
    return "${tenths / 600}:${seconds.toString().padStart(2, '0')}.${tenths % 10}"
}

/** This value rounded to tenths, as `-62.3`. */
private fun Double.toTenthsText() = ((this * 10).roundToInt() / 10.0).toString()

// the distance a press on the window region travels before it becomes a drag
private const val DRAG_THRESHOLD_PX = 3

// the distance a press on a lane travels, clearly sideways, before it moves the sample
private const val SAMPLE_DRAG_THRESHOLD_PX = 8
