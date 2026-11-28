package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.Css
import koala.modifier.Modifier
import koala.modifier.modify
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.ui.HeadShape
import streetlight.model.music.ui.NoteLayout
import web.html.HTMLElement

/**
 * Renders [note] with [mod] at its onset and step: its heads, with any bars of its value above and below them, its dot,
 * its ledger lines, and a sharp when it sits between degrees of the scale.
 */
fun ViewScope.note(note: NoteLayout, mod: Modifier? = null): HTMLElement {
    val span = note.heads.maxOf { it.step }
    val place = modify(
        Css.Left.of((note.onset * 100).pct),
        Css.Bottom.of(LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * note.step} - var(--unit))")),
        Css.Height.of(LinearDimension("calc(var(--unit-2) + var(--unit) * ${STAFF_STEP_UNITS * span})")),
    )
    return div(modify(mod, MeasureStyle.Note, note.value?.shapeMod, place)) {
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

//context(source: NotationSource, notes: List<Note>)
//fun ViewScope.note(noteIndex: Int, measureTick: Int, measureDuration: Int, mod: Modifier? = null): HTMLElement {
//    val note = notes[noteIndex]
//    val chord = note.toChord(source.key)
//    val step = chord.min()
//    val heads = chord.map { NoteHead(it - step, HeadShape.Round) }
//    val span = heads.maxOf { it.step }
//    val measureOnset = note.toMeasureOnset(measureTick, measureDuration)
//    val place = modify(
//        Css.Left.of((measureOnset * 100).pct),
//        Css.Bottom.of(LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * step} - var(--unit))")),
//        Css.Height.of(LinearDimension("calc(var(--unit-2) + var(--unit) * ${STAFF_STEP_UNITS * span})")),
//    )
//    return div(modify(mod, MeasureStyle.Note, note.value?.shapeMod, place)) {
//        heads.forEach { head ->
//            val crossMod = MeasureStyle.CrossHead.takeIf { head.shape == HeadShape.Cross }
//            div(modify(MeasureStyle.Head, crossMod, Css.Bottom.of(stepsHeight(head.step))))
//        }
//        if (note.isDotted) div(MeasureStyle.Dot)
//        if (note.isRaised) textBlock("♯", MeasureStyle.Accidental)
//        note.ledgers.forEach { steps ->
//            val bottom = LinearDimension("calc(var(--unit) * ${1 + STAFF_STEP_UNITS * steps} - 1px)")
//            div(modify(MeasureStyle.Ledger, Css.Bottom.of(bottom)))
//        }
//    }
//}
//
//fun ViewScope.note(
//    measureOnset: Double,
//    step: Int,
//    span: Int,
//    value: NoteValue?,
//    notes: List<Int>,
//    head: HeadShape,
//    isDotted: Boolean,
//    isRaised: Boolean,
//    ledgers: List<Int>,
//    mod: Modifier?,
//): HTMLElement {
//    val shapeMod = value?.shapeMod
//    val place = modify(
//        Css.Left.of((measureOnset * 100).pct),
//        Css.Bottom.of(LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * step} - var(--unit))")),
//        Css.Height.of(LinearDimension("calc(var(--unit-2) + var(--unit) * ${STAFF_STEP_UNITS * span})")),
//    )
//
//    return div(modify(mod, MeasureStyle.Note, shapeMod, place)) {
//        notes.forEach { note ->
//            val crossMod = MeasureStyle.CrossHead.takeIf { head == HeadShape.Cross }
//            div(modify(MeasureStyle.Head, crossMod, Css.Bottom.of(stepsHeight(note))))
//        }
//        if (isDotted) div(MeasureStyle.Dot)
//        if (isRaised) textBlock("♯", MeasureStyle.Accidental)
//        ledgers.forEach { steps ->
//            val bottom = LinearDimension("calc(var(--unit) * ${1 + STAFF_STEP_UNITS * steps} - 1px)")
//            div(modify(MeasureStyle.Ledger, Css.Bottom.of(bottom)))
//        }
//    }
//}