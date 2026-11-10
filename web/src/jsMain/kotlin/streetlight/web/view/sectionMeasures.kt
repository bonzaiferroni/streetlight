package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.ui.HeadShape
import streetlight.model.music.ui.NoteNotation
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.SyllableNotation
import web.html.HTMLElement

/**
 * Renders the measures of [notation] as a strip with [mod]: a staff crossed by each measure's bar when it has one, then
 * its notes and syllables. With [onNote], the notes with a place in their track can be grabbed, and each note is handed
 * over with its element.
 */
fun ViewScope.sectionMeasures(
    notation: SectionNotation,
    mod: Modifier? = null,
    onNote: ((NoteNotation, HTMLElement) -> Unit)? = null,
) {
    fun ViewScope.placeNote(note: NoteNotation) {
        val element = note(note, MeasureStyle.Grabbable.takeIf { onNote != null && note.source != null })
        onNote?.invoke(note, element)
    }
    div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(notation.steps)))) {
        if (notation.hasStaff) staff(notation)
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Measure, Css.Width.of((measure.share * 100).pct))) {
                measure.notes.forEach { placeNote(it) }
                measure.syllables.forEach { syllable(it) }
            }
        }
        notation.pickup.forEach { placeNote(it) }
        notation.pickupSyllables.forEach { syllable(it) }
    }
}

/**
 * Renders the staff of [notation] a sixteenth ahead of its section, so each downbeat's note falls within its measure,
 * with a bar at each end. A bar between measures comes closer to its downbeat to stay halfway from a nearer last note.
 */
private fun ViewScope.staff(notation: SectionNotation) {
    val bottom = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * notation.staffStep} - 1px)")
    val leadShare = notation.staffLeadShare
    val place = modify(Css.Left.of((-leadShare * 100).pct), Css.Right.of((leadShare * 100).pct), Css.Bottom.of(bottom))
    div(modify(MeasureStyle.Staff, place)) {
        bar(0.0)
        var end = 0.0
        notation.measures.dropLast(1).forEach { measure ->
            end += measure.share
            val lastOnset = measure.notes.maxOfOrNull { it.onset } ?: 0.0
            bar(end + leadShare - minOf(leadShare, (1 - lastOnset) * measure.share / 2))
        }
        bar(1.0)
    }
}

/** Renders a bar [share] of the way across its staff. */
private fun ViewScope.bar(share: Double) = div(modify(MeasureStyle.Bar, Css.Left.of((share * 100).pct)))

/** Renders [syllable] at its onset, lined up with the left edge of a note's head there. */
private fun ViewScope.syllable(syllable: SyllableNotation) =
    textBlock(syllable.text, modify(MeasureStyle.Syllable, Css.Left.of((syllable.onset * 100).pct)))

/**
 * Renders [note] with [mod] at its onset and step: its heads, with any bars of its value above and below them, its dot,
 * its ledger lines, and a sharp when it sits between degrees of the scale.
 */
fun ViewScope.note(note: NoteNotation, mod: Modifier? = null): HTMLElement {
    val span = note.heads.maxOf { it.step }
    val place = modify(
        Css.Left.of((note.onset * 100).pct),
        Css.Bottom.of(LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * note.step} - var(--unit))")),
        Css.Height.of(LinearDimension("calc(var(--unit-2) + var(--unit) * ${STAFF_STEP_UNITS * span})")),
    )
    return div(modify(mod, MeasureStyle.Note, note.value?.shape, place)) {
        note.heads.forEach { head ->
            val crossMod = MeasureStyle.CrossHead.takeIf { head.shape == HeadShape.Cross }
            div(modify(MeasureStyle.Head, crossMod, Css.Bottom.of(stepsHeight(head.step))))
        }
        if (note.isDotted) div(MeasureStyle.Dot)
        if (note.isRaised) textBlock("♯", MeasureStyle.Accidental)
        note.ledgers.forEach { steps ->
            val bottom = LinearDimension("calc(var(--unit) * ${1 + STAFF_STEP_UNITS * steps} - 1px)")
            div(modify(MeasureStyle.Ledger, Css.Bottom.of(bottom)))
        }
    }
}

/** The modifiers that draw a note of this value. */
val NoteValue.shape get() = when (this) {
    NoteValue.Whole -> MeasureStyle.OpenNote
    NoteValue.Half -> modify(MeasureStyle.OpenNote, MeasureStyle.StemNote)
    NoteValue.Quarter -> MeasureStyle.StemNote
    NoteValue.Eighth -> MeasureStyle.OneFlagNote
    NoteValue.Sixteenth -> MeasureStyle.TwoFlagNote
}

/** The height of [steps] staff steps, two to a staff space. */
fun stepsHeight(steps: Int) = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * steps})")
