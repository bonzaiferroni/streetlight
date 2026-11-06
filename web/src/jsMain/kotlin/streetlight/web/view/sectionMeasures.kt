package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.web.model.NoteNotation
import streetlight.web.model.NoteValue
import streetlight.web.model.SectionNotation

/** Renders the measures of [notation] as a strip with [mod], each opened by a vertical line and holding its notes. */
fun ViewScope.sectionMeasures(notation: SectionNotation, mod: Modifier? = null) {
    div(modify(mod, MeasureStyle.Strip, Css.Height.of(stepsHeight(notation.steps)))) {
        notation.measures.forEach { measure ->
            div(modify(MeasureStyle.Measure, Css.Width.of((measure.share * 100).pct))) {
                measure.notes.forEach { note(it) }
            }
        }
    }
}

/** Renders [note] at its onset and step: a head with a stem as tall as itself, and its dot. */
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
    }
}

/** The height of [steps] staff steps, two to a staff space, which a note head fills. */
fun stepsHeight(steps: Int) = LinearDimension("calc(var(--unit) * $steps)")
