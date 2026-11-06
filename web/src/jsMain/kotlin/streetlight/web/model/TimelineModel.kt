package streetlight.web.model

import kampfire.model.Tap

/** The state behind a timeline: a cursor, and the [TimeWindow] of the whole composition that the lanes below it show. */
interface TimelineModel {
    /** The end of playback, or `null` with nothing to play. */
    val endSecondsState: Tap<Double?>

    /** The length of the whole timeline. */
    val totalSecondsState: Tap<Double>
    val windowState: Tap<TimeWindow>
    val cursorSecondsState: Tap<Double>

    /** Moves the cursor to [seconds] on the timeline, stopping playback. */
    fun moveCursor(seconds: Double)

    /** Moves the window to start at [seconds], keeping it within the composition. */
    fun moveWindow(seconds: Double)

    /** Sets the length of the window to [spanSeconds], keeping its start. */
    fun resizeWindow(spanSeconds: Double)
}
