package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.web.model.NoteNotation
import streetlight.web.model.NoteValue
import streetlight.web.model.SectionNotation

/** Renders the measures of [notation] as a strip with [mod]: a staff crossed by each measure's bar, then its notes. */
fun ViewScope.sectionMeasures(notation: SectionNotation, mod: Modifier? = null) {
    div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(notation.steps)))) {
        staff(notation)
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Measure, Css.Width.of((measure.share * 100).pct))) {
                measure.notes.forEach { note(it) }
            }
        }
        notation.pickup.forEach { note(it) }
    }
}

/** Renders the staff of [notation], with a bar at the start of each measure and one closing it. */
private fun ViewScope.staff(notation: SectionNotation) {
    val bottom = LinearDimension("calc(var(--unit) * ${notation.staffStep} - 1px)")
    div(modify(MeasureStyle.Staff, Css.Bottom.of(bottom))) {
        var start = 0.0
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Bar, Css.Left.of((start * 100).pct)))
            start += measure.share
        }
    }
}

/** Renders [note] at its onset and step: a head with the bars of its value above and below it, its dot and its ledger lines. */
private fun ViewScope.note(note: NoteNotation) {
    val shape = when (note.value) {
        NoteValue.Whole -> MeasureStyle.OpenNote
        NoteValue.Half -> modify(MeasureStyle.OpenNote, MeasureStyle.StemNote)
        NoteValue.Quarter -> MeasureStyle.StemNote
        NoteValue.Eighth -> MeasureStyle.OneFlagNote
        NoteValue.Sixteenth -> MeasureStyle.TwoFlagNote
    }
    val place = modify(Css.Left.of((note.onset * 100).pct), Css.Bottom.of(stepsHeight(note.step)))
    div(modify(MeasureStyle.Note, shape, place)) {
        if (note.isDotted) div(MeasureStyle.Dot)
        note.ledgers.forEach { steps ->
            val bottom = LinearDimension("calc(50% + var(--unit) * $steps - 1px)")
            div(modify(MeasureStyle.Ledger, Css.Bottom.of(bottom)))
        }
    }
}

/** The height of [steps] staff steps, two to a staff space, which a note head fills. */
fun stepsHeight(steps: Int) = LinearDimension("calc(var(--unit) * $steps)")
