package streetlight.web.view

import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.pct
import kotlinx.css.px
import streetlight.model.music.Beats
import kotlinx.css.LinearDimension
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.lines
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.MODE_NAMES
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicForm
import streetlight.web.model.TONIC_NAMES
import kampfire.model.combine
import streetlight.model.music.Music
import streetlight.model.music.MusicSection
import streetlight.model.music.SectionId
import streetlight.web.dom.layout.actionCard
import streetlight.web.model.MixEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.TrackPlayer
import streetlight.web.model.toaster
import kampfire.model.toDataOr
import kotlin.uuid.Uuid

/** Renders the song's music, the style it is written in and its tracks. */
context(studio: MusicStudio, model: MusicEditor, mix: MixEditor, player: TrackPlayer)
fun ViewScope.musicEditor() = column {
    row {
        dropMenu(studio.notationStyleState)
        dropMenu(studio.notationViewState)
        switch("Lyrics", studio.isLyricsShownState)
    }
    musicSection()
    trackEditor()
}

/** Renders the music's properties, with an action to edit them, above the arrangement every track follows. */
context(model: MusicEditor)
private fun ViewScope.musicSection() {
    val editId = Id(Uuid.random().toString())
    musicEditPopover(editId)
    actionSection("Music", listOf(MenuAction("Edit", config = { setPopoverTarget(editId, true) }) { })) {
        flowBlock(model.musicState) { music -> textBlock(music.summary, modify(TextSmall, InkDimFg)) }
        actionCard("Arrangement") {
            val picksState = model.arrangementState.combine(model.sectionsState) { picks, choices -> picks to choices }
            flowBlock(picksState) { (picks, choices) ->
                sectionPicks(picks, choices) { model.setMusicArrangement(it) }
            }
        }
    }
}

/** A popover with [id] holding the key, time, tempo and capo of the music, which its save button applies. */
context(model: MusicEditor)
private fun ViewScope.musicEditPopover(id: Id) {
    popover(id) { popover ->
        val form = MusicForm(model.musicState.now)
        column(Padding(2)) {
            row {
                dropMenu(TONIC_NAMES, form.tonicState)
                dropMenu(MODE_NAMES, form.modeState)
            }
            row {
                textField(form.timeTextState, "time", size = 4)
                textField(form.bpmTextState, "tempo", size = 4)
                textField(form.capoTextState, "capo", size = 4)
            }
            button("Save", {
                val music = form.readOnto(model.musicState.now).toDataOr(toaster) { return@button }
                model.musicState.set(music)
                popover.close()
            })
        }
    }
}

/**
 * Renders [picks] in order, each a button that takes it out, then a button that opens a menu of [choices] to add one.
 * Each change is handed to [onChange].
 */
fun ViewScope.sectionPicks(
    picks: List<MusicSection>,
    choices: List<MusicSection>,
    onChange: (List<SectionId>) -> Unit,
) {
    val menuId = Id(Uuid.random().toString())
    val sectionIds = picks.map { it.sectionId }
    popover(menuId) { popover ->
        actionMenu(choices.map { choice ->
            MenuAction(choice.name) {
                popover.close()
                onChange(sectionIds + choice.sectionId)
            }
        })
    }
    row(modify(AlignItemsCenter, FlexWrap, Flex1)) {
        picks.forEachIndexed { index, pick ->
            button("${pick.name} ×", { onChange(sectionIds.filterIndexed { at, _ -> at != index }) }, TextSmall)
        }
        button("Add", mod = TextSmall, block = { setPopoverTarget(menuId, true) })
    }
}

/** A popover with [id] holding a field for a name and a button labeled [label], which closes it once [onSubmit] takes the name. */
fun ViewScope.namePopover(id: Id, label: String, onSubmit: (String) -> Boolean) {
    popover(id) { popover ->
        val nameState = storeOf("")
        val submit = {
            if (onSubmit(nameState.now)) {
                nameState.set("")
                popover.close()
            }
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(nameState, "name", Flex1, onEnterSubmit = submit)
            button(label, submit)
        }
    }
}

/** The strips of [section], a line of its text to each. */
fun ViewScope.sectionStrips(section: SectionNotation) {
    val lines = section.lines()
    // the pickup and staff hang off the left of each strip, so a margin as wide as the widest keeps them in the pane
    val leadTicks = lines.maxOfOrNull { maxOf(it.pickupShare, it.staffLeadShare) * it.ticks } ?: 0.0
    val paneTicks = section.measuresPerLine * section.measureTicks + leadTicks
    // a line of few measures would stretch across a wide pane, so a beat takes no more than its widest
    val maxWidth = LinearDimension("calc(var(--unit) * ${paneTicks / Beats.TICKS_PER_BEAT * MAX_BEAT_UNITS})")
    // the first head and its sharp reach left of their onset by a fixed width, which no margin of ticks covers
    val headRoom = LinearDimension("calc(var(--unit) * $HEAD_ROOM_UNITS)")
    column(modify(Css.MaxWidth.of(maxWidth), Css.Gap.of(2.px))) {
        lines.forEach { line ->
            val place = modify(
                Css.MarginLeft.of((leadTicks / paneTicks * 100).pct),
                Css.Width.of((line.ticks / paneTicks * 100).pct),
            )
            // each line on a band of its own, the band reaching the whole width however short the line
            div(modify(ZenBg, Css.PaddingLeft.of(headRoom))) { sectionMeasures(line, place) }
        }
    }
}

/** The key, time, tempo and capo of this music, as `G Major · 3/4 · 90 bpm · capo 2`. */
private val Music.summary get() = listOfNotNull(
    "${TONIC_NAMES[key.tonic.value]} ${key.mode.name}",
    "${time.beats}/${time.beatUnit}",
    "$bpm bpm",
    capo?.let { "capo $it" },
).joinToString(" · ")

/** The widest a beat of a section's notation is drawn, in units. */
private const val MAX_BEAT_UNITS = 6

/** The room left of a section's notation for half a note's head and a sharp before it, in units. */
private const val HEAD_ROOM_UNITS = 2.4
