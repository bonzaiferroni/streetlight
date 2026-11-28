package streetlight.web.view

import kampfire.model.tapOf
import koala.dom.ViewScope
import koala.dom.flowModifier
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.ui.MeasureLayout
import streetlight.model.music.ui.NoteLayout
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.NotationLayout
import streetlight.model.music.ui.VocalLayout
import streetlight.web.model.MusicConfig
import web.html.HTMLElement

/**
 * Renders the measures of [notation] as strips with [mod]: a staff when it has one, then a strip for each line of its
 * vocals, which [config] hides with the lyrics unless the notation is vocals alone. With [onNote], the notes with a
 * place in their track can be grabbed, and each note is handed over with its element.
 */
context(config: MusicConfig)
fun ViewScope.notationMeasures(
    notation: NotationLayout,
    mod: Modifier? = null,
    onNote: ((NoteLayout, HTMLElement) -> Unit)? = null,
) {
    if (!notation.hasStaff) return vocalStrips(notation, mod)
    staffStrip(notation, mod, onNote)
    div(null) { vocalStrips(notation, mod) }
        .flowModifier(config.isLyricsShownState.tapOf { !it }, DisplayNone, contentScope)
}

/**
 * Renders the staff of [notation] as a strip with [mod], crossed by each measure's bar, then its notes, each handed to
 * [onNote] as [notationMeasures] does.
 */
private fun ViewScope.staffStrip(
    notation: NotationLayout,
    mod: Modifier?,
    onNote: ((NoteLayout, HTMLElement) -> Unit)?,
) {
    fun ViewScope.placeNote(note: NoteLayout) {
        val element = note(note, MeasureStyle.Grabbable.takeIf { onNote != null && note.source != null })
        onNote?.invoke(note, element)
    }
    div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(notation.steps)))) {
        staff(notation)
        notation.measures.forEach { measure ->
            div(measure.mod) { measure.notes.forEach { placeNote(it) } }
        }
    }
}

/** Renders each line of the vocals of [notation] as a strip with [mod]. */
private fun ViewScope.vocalStrips(notation: NotationLayout, mod: Modifier?) {
    repeat(notation.vocalLines) { line ->
        div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(VOCAL_LINE_STEPS)))) {
            notation.measures.forEach { measure ->
                div(measure.mod) { measure.vocals[line].forEach { syllable(it) } }
            }
        }
    }
}

/**
 * Renders the staff of [notation] a sixteenth ahead of its section, so each downbeat's note falls within its measure,
 * with a bar at each end. A bar between measures comes closer to its downbeat to stay halfway from a nearer last note.
 */
private fun ViewScope.staff(notation: NotationLayout) {
    val bottom = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * notation.staffStep} - 1px)")
    val leadShare = notation.staffLeadShare
    val place = modify(Css.Left.of((-leadShare * 100).pct), Css.Right.of((leadShare * 100).pct), Css.Bottom.of(bottom))
    div(modify(MeasureStyle.Staff, place)) {
        bar(0.0)
        var end = 0.0
        notation.measures.filterNot { it.isPickup }.dropLast(1).forEach { measure ->
            end += measure.width
            val lastOnset = measure.notes.maxOfOrNull { it.onset } ?: 0.0
            bar(end + leadShare - minOf(leadShare, (1 - lastOnset) * measure.width / 2))
        }
        bar(1.0)
    }
}

/** The modifiers that place this measure across its strip, a pickup hanging off its left. */
private val MeasureLayout.mod
    get() = modify(
        MeasureStyle.Measure,
        MeasureStyle.PickupMeasure.takeIf { isPickup },
        Css.Width.of((width * 100).pct)
    )

/** Renders a bar [share] of the way across its staff. */
private fun ViewScope.bar(share: Double) = div(modify(MeasureStyle.Bar, Css.Left.of((share * 100).pct)))

/** Renders [syllable] at its onset, mid-strip, lined up with the left edge of a note's head there. */
private fun ViewScope.syllable(syllable: VocalLayout) {
    val place = modify(Css.Left.of((syllable.onset * 100).pct), Css.Bottom.of(stepsHeight(VOCAL_LINE_STEPS / 2)))
    textBlock(syllable.text, modify(MeasureStyle.Syllable, place))
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
