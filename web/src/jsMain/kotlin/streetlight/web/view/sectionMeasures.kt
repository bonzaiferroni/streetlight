package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.web.model.NoteNotation
import streetlight.web.model.NoteValue
import streetlight.web.model.SectionNotation
import web.html.HTMLElement

/**
 * Renders the measures of [notation] as a strip with [mod]: a staff crossed by each measure's bar, then its notes. With
 * [onNote], the notes can be grabbed, and each is handed over with its element and the strip's.
 */
fun ViewScope.sectionMeasures(
    notation: SectionNotation,
    mod: Modifier? = null,
    onNote: ((NoteNotation, HTMLElement, HTMLElement) -> Unit)? = null,
) {
    val grab = MeasureStyle.Grabbable.takeIf { onNote != null }
    val notes = mutableListOf<Pair<NoteNotation, HTMLElement>>()
    val strip = div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(notation.steps)))) {
        staff(notation)
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Measure, Css.Width.of((measure.share * 100).pct))) {
                measure.notes.forEach { notes += it to note(it, grab) }
            }
        }
        notation.pickup.forEach { notes += it to note(it, grab) }
    }
    if (onNote != null) notes.forEach { (note, element) -> onNote(note, element, strip) }
}

/** Renders the staff of [notation], with a bar at the start of each measure and one closing it. */
private fun ViewScope.staff(notation: SectionNotation) {
    val bottom = LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * notation.staffStep} - 1px)")
    div(modify(MeasureStyle.Staff, Css.Bottom.of(bottom))) {
        var start = 0.0
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Bar, Css.Left.of((start * 100).pct)))
            start += measure.share
        }
    }
}

/**
 * Renders [note] with [mod] at its onset and step: a head with the bars of its value above and below it, its dot, its
 * ledger lines, and a sharp when it sits between degrees of the scale.
 */
fun ViewScope.note(note: NoteNotation, mod: Modifier? = null): HTMLElement {
    val place = modify(Css.Left.of((note.onset * 100).pct), Css.Bottom.of(stepsHeight(note.step)))
    return div(modify(mod, MeasureStyle.Note, note.value.shape, place)) {
        if (note.isDotted) div(MeasureStyle.Dot)
        if (note.isRaised) textBlock("♯", MeasureStyle.Accidental)
        note.ledgers.forEach { steps ->
            val bottom = LinearDimension("calc(50% + var(--unit) * ${STAFF_STEP_UNITS * steps} - 1px)")
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
