package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.dom.component.textBlock
import koala.modifier.Css
import koala.modifier.Modifier
import koala.modifier.modify
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.Key
import streetlight.model.music.ui.ChordEvent
import streetlight.model.music.ui.DrumEvent
import streetlight.model.music.ui.HeadShape
import streetlight.model.music.ui.NotationEvent
import streetlight.model.music.ui.NoteEvent
import streetlight.model.music.ui.StaffWindow
import streetlight.model.music.ui.headShapeOf
import streetlight.model.music.ui.isDottedOf
import streetlight.model.music.ui.isRaisedOf
import streetlight.model.music.ui.ledgersOf
import streetlight.model.music.ui.midiChordOf
import streetlight.model.music.ui.noteValueOf
import streetlight.model.music.ui.staffStepOf
import web.html.HTMLElement

/**
 * Renders [event] with [mod] [onset] across its measure, within [window] in [key]: its heads, with any bars of its value
 * above and below them, its dot, its ledger lines, and a sharp when it sits between degrees of the scale. A drum is drawn
 * as its head alone. Returns `null` for a rest or a syllable, which draw nothing on the staff.
 */
fun ViewScope.note(event: NotationEvent, onset: Double, window: StaffWindow, key: Key, mod: Modifier? = null): HTMLElement? {
    val midis = when (event) {
        is NoteEvent -> listOfNotNull(event.midi)
        is ChordEvent -> event.chord?.let { midiChordOf(it, key) }.orEmpty()
        else -> emptyList()
    }
    val drum = (event as? DrumEvent)?.takeIf { it.hit != null }?.drum
    val staffSteps = drum?.let { listOf(staffStepOf(it)) } ?: midis.map { staffStepOf(it, key) }
    if (staffSteps.isEmpty()) return null
    val lowest = staffSteps.min()
    val ticks = event.beats.ticks
    val value = noteValueOf(ticks).takeIf { drum == null }

    val place = modify(
        Css.Left.of((onset * 100).pct),
        Css.Bottom.of(LinearDimension("calc(var(--unit) * ${STAFF_STEP_UNITS * window.stepOf(lowest)} - var(--unit))")),
        Css.Height.of(LinearDimension("calc(var(--unit-2) + var(--unit) * ${STAFF_STEP_UNITS * (staffSteps.max() - lowest)})")),
    )
    return div(modify(mod, MeasureStyle.Note, value?.shapeMod, place)) {
        staffSteps.forEach { step ->
            val crossMod = MeasureStyle.CrossHead.takeIf { drum?.let(::headShapeOf) == HeadShape.Cross }
            div(modify(MeasureStyle.Head, crossMod, Css.Bottom.of(stepsHeight(step - lowest))))
        }
        if (value != null && isDottedOf(ticks)) div(MeasureStyle.Dot)
        if (midis.firstOrNull()?.let { isRaisedOf(it, key) } == true) textBlock("♯", MeasureStyle.Accidental)
        ledgersOf(lowest, staffSteps.max()).forEach { ledger ->
            val bottom = LinearDimension("calc(var(--unit) * ${1 + STAFF_STEP_UNITS * (ledger - lowest)} - 1px)")
            div(modify(MeasureStyle.Ledger, Css.Bottom.of(bottom)))
        }
    }
}
