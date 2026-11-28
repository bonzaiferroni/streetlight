package streetlight.web.view

import kampfire.model.tapOf
import koala.dom.ViewScope
import koala.dom.flowModifier
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.TrackType
import streetlight.model.music.ui.ChordEvent
import streetlight.model.music.ui.DrumEvent
import streetlight.model.music.ui.MeasureLine
import streetlight.model.music.ui.MusicSource
import streetlight.model.music.ui.NotationEvent
import streetlight.model.music.ui.NoteEvent
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.TrackSource
import streetlight.model.music.ui.VocalEvent
import streetlight.model.music.ui.eventsOf
import streetlight.model.music.ui.onsetOf
import streetlight.web.model.MusicConfig
import web.html.HTMLElement

/**
 * Renders the measures of [line] of [track] in [source] as strips with [mod]: a staff when the track has one, then a
 * strip for its vocals, which [config] hides with the lyrics unless the track is vocals alone. With [onNote], the notes
 * with a place in their track can be grabbed, and each note is handed over with its element.
 */
context(config: MusicConfig)
fun ViewScope.notationMeasures(
    source: MusicSource,
    track: TrackSource,
    line: MeasureLine,
    mod: Modifier? = null,
    onNote: ((NotationEvent, HTMLElement) -> Unit)? = null,
) {
    if (track.trackType == TrackType.Lyrics) return vocalStrips(source, track, line, mod)
    staffStrip(source, track, line, mod, onNote)
    if (track.events.none { it is VocalEvent }) return
    div(null) { vocalStrips(source, track, line, mod) }
        .flowModifier(config.isLyricsShownState.tapOf { !it }, DisplayNone, contentScope)
}

/** The ticks of [line] from its first downbeat, which its measures share, a pickup hanging off its left. */
fun MusicSource.downbeatTicksOf(line: MeasureLine) =
    measureTickOf(line.measureEnd) - measureTickOf(maxOf(line.measureStart, 1))

/**
 * Renders the staff of [line] as a strip with [mod], crossed by each measure's bar, then its notes, each handed to
 * [onNote] as [notationMeasures] does.
 */
private fun ViewScope.staffStrip(
    source: MusicSource,
    track: TrackSource,
    line: MeasureLine,
    mod: Modifier?,
    onNote: ((NotationEvent, HTMLElement) -> Unit)?,
) {
    val window = track.layout.window
    div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(window.steps)))) {
        staff(source, track, line)
        for (index in line.measureStart until line.measureEnd) {
            div(source.measureMod(line, index)) {
                track.eventsOf(index).forEach { event ->
                    val grabbable = MeasureStyle.Grabbable.takeIf { onNote != null && event !is DrumEvent }
                    val element = note(event, source.onsetOf(event, index), window, source.key, grabbable)
                    if (element != null) onNote?.invoke(event, element)
                }
            }
        }
    }
}

/**
 * Renders the vocals of [line] as strips with [mod]: one for the song, or one for each section sung to a target, as its
 * verses would overlap.
 */
private fun ViewScope.vocalStrips(source: MusicSource, track: TrackSource, line: MeasureLine, mod: Modifier?) {
    val vocals = track.events.filterIsInstance<VocalEvent>()
    val sectionIds = if (source.targetId == null) listOf(null) else vocals.map { it.sectionId }.distinct()
    sectionIds.forEach { sectionId ->
        div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(VOCAL_LINE_STEPS)))) {
            for (index in line.measureStart until line.measureEnd) {
                div(source.measureMod(line, index)) {
                    track.eventsOf(index).filterIsInstance<VocalEvent>()
                        .filter { sectionId == null || it.sectionId == sectionId }
                        .forEach { vocal -> vocal.text?.let { syllable(it, source.onsetOf(vocal, index)) } }
                }
            }
        }
    }
}

/**
 * Renders the staff of [line] a sixteenth ahead of its first downbeat, so each downbeat's note falls within its
 * measure, with a bar at each end. A bar between measures comes closer to its downbeat to stay halfway from a nearer
 * last note.
 */
private fun ViewScope.staff(source: MusicSource, track: TrackSource, line: MeasureLine) {
    val lineTicks = source.downbeatTicksOf(line).toDouble()
    val bottom = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * track.layout.window.staffStep} - 1px)")
    val leadShare = NoteValue.Sixteenth.ticks / lineTicks
    val place = modify(Css.Left.of((-leadShare * 100).pct), Css.Right.of((leadShare * 100).pct), Css.Bottom.of(bottom))
    div(modify(MeasureStyle.Staff, place)) {
        bar(0.0)
        var end = 0.0
        for (index in maxOf(line.measureStart, 1) until line.measureEnd - 1) {
            val width = source.measureBeatsOf(index).ticks / lineTicks
            end += width
            val lastOnset = track.eventsOf(index).filter { it.isHead }.maxOfOrNull { source.onsetOf(it, index) } ?: 0.0
            bar(end + leadShare - minOf(leadShare, (1 - lastOnset) * width / 2))
        }
        bar(1.0)
    }
}

/** Whether this event draws a head on the staff, as a note, chord or drum that is not a rest. */
private val NotationEvent.isHead
    get() = when (this) {
        is NoteEvent -> midi != null
        is ChordEvent -> chord != null
        is DrumEvent -> hit != null
        is VocalEvent -> false
    }

/** The modifiers that place measure [index] across the strip of [line], a pickup hanging off its left. */
private fun MusicSource.measureMod(line: MeasureLine, index: Int) = modify(
    MeasureStyle.Measure,
    MeasureStyle.PickupMeasure.takeIf { index == 0 },
    Css.Width.of((measureBeatsOf(index).ticks.toDouble() / downbeatTicksOf(line) * 100).pct),
)

/** Renders a bar [share] of the way across its staff. */
private fun ViewScope.bar(share: Double) = div(modify(MeasureStyle.Bar, Css.Left.of((share * 100).pct)))

/** Renders [text] [onset] across its measure, mid-strip, lined up with the left edge of a note's head there. */
private fun ViewScope.syllable(text: String, onset: Double) {
    val place = modify(Css.Left.of((onset * 100).pct), Css.Bottom.of(stepsHeight(VOCAL_LINE_STEPS / 2)))
    textBlock(text, modify(MeasureStyle.Syllable, place))
}

/** The modifiers that draw a note of this value. */
val NoteValue.shapeMod
    get() = when (this) {
        NoteValue.Whole -> MeasureStyle.OpenNote
        NoteValue.Half -> modify(MeasureStyle.OpenNote, MeasureStyle.StemNote)
        NoteValue.Quarter -> MeasureStyle.StemNote
        NoteValue.Eighth -> MeasureStyle.OneFlagNote
        NoteValue.Sixteenth -> MeasureStyle.TwoFlagNote
    }

/** The height of [steps] staff steps, two to a staff space. */
fun stepsHeight(steps: Int) = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * steps})")

/** The staff steps a line of vocals takes. */
private const val VOCAL_LINE_STEPS = 4
