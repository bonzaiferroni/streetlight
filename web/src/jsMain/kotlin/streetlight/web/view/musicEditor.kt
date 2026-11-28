package streetlight.web.view

import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.SvgFile
import koala.model.HeadingLevel
import koala.model.IconAction
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.pct
import kotlinx.css.px
import streetlight.model.music.Beats
import kotlinx.css.LinearDimension
import streetlight.model.music.ui.MusicSource
import streetlight.model.music.ui.musicSourceOf
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.TrackSource
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.MODE_NAMES
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicForm
import streetlight.web.model.MusicFormProblem
import streetlight.web.model.TONIC_NAMES
import streetlight.model.music.Music
import streetlight.model.music.Section
import streetlight.web.model.WaveShelf
import streetlight.web.model.NotationFocus
import streetlight.web.model.TrackFocus
import streetlight.model.studio.StudioDb
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicPlayer
import streetlight.web.model.musicPlanOf
import streetlight.web.model.toaster
import kampfire.model.toDataOr
import koala.component.IconStyle
import koala.component.heading5
import koala.dom.layout.OrderHandle
import koala.dom.layout.orderColumn
import kotlinx.coroutines.launch
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.TrackType
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.iconActionsRow
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.model.MusicHost
import streetlight.web.model.readCapoText
import kotlin.uuid.Uuid

/** Renders the song's arrangement beside its tracks, or the editor of the track or notation in focus alone. */
context(config: MusicConfig, host: MusicHost, db: StudioDb, waveShelf: WaveShelf, player: MusicPlayer)
fun ViewScope.musicEditor() {
    val model = MusicEditor(contentScope, host)
    context(model) {
        flowBlock(model.focusState, Transitioner) { focus ->
            when (focus) {
                null -> column {
                    musicTable(model::setFocus)
                    formRow {
                        musicSection()
                        trackList()
                    }
                }
                is TrackFocus -> {

                    trackEditor(focus, model::setFocus)
                }
                is NotationFocus -> notationEditor(focus, model::setFocus)
            }
        }
    }
}

/** Renders the tracks of the song under an action that adds one, each a row that brings its editor into focus. */
context(config: MusicConfig, model: MusicEditor, waveShelf: WaveShelf, player: MusicPlayer)
fun ViewScope.trackList() {
    val newId = Id(Uuid.random().toString())
    popover(newId) { popover ->
        actionMenu(TrackType.entries.map { trackType ->
            MenuAction(trackType.name) {
                model.addTrack(trackType)
                popover.close()
            }
        })
    }
    actionSection(
        label = "Tracks",
        level = HeadingLevel.H4,
        icons = listOf(IconAction(SvgFile.Plus.small, "New", config = { setPopoverTarget(newId, true) }) { }),
    ) {
        // keyed by id, so an edit to a track's content or name or a move leaves its row in place
        orderColumn(model.tracksState, { it.trackId }, model::setTrackOrder, Transitioner) { track -> trackRow(track) }
        flowBlock(model.tracksState.tapOf { it.isEmpty() }) { isEmpty ->
            if (isEmpty) textBlock("No tracks yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
        }
    }
}

/**
 * Renders the row of [track], which brings its editor into focus, with a grip that moves it among the tracks.
 */
context(handle: OrderHandle, config: MusicConfig, model: MusicEditor, waveShelf: WaveShelf, player: MusicPlayer)
private fun ViewScope.trackRow(track: Track) {
    val trackId = track.trackId
    val nameId = Id(Uuid.random().toString())
    val key = trackId.toString()
    trackEditPopover(nameId, track)
    // named apart from its item, so it pops in with the tab as its item would, and pairs with its focus card
    row(modify(AlignItemsCenter, track.trackType.color.bg, Padding(1), transition("card-$key"), Presto.Pop)) {
        handle.grip(icon(SvgFile.GripVertical.small, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Move"))))
        button({ model.readTrack(trackId)?.let { model.setFocus(TrackFocus(trackId)) } }, Flex1) {
            row(AlignItemsCenter) {
                icon(track.trackType.svg.large, Height(5))
                heading5(track.name)
            }
        }
        // a lyric track makes no sound, so it has nothing to play
        val play = MenuAction("Play") {
            if (player.isPlayingState.now) player.stop()
            else model.readTrack(trackId)?.let { track ->
                contentScope.launch {
                    val sheet = model.sheetState.now
                    sheet.musicSourceOf(null, track.trackId).musicPlanOf(listOf(track.trackId), waveShelf.musicWavesOf(sheet))?.let {
                        player.play(it)
                    }
                }
            }
        }.takeIf { track !is LyricTrack }
        menuActionsRow(listOfNotNull(
            play,
            MenuAction("Remove") { model.removeTrack(trackId) },
        ))
        val edit = IconAction(SvgFile.Gear.small, "Edit", config = { setPopoverTarget(nameId, true) }) { }
        iconActionsRow(listOf(edit), SmallIconHeight)
    }
}

/**
 * A popover with [id] holding the name and capo of [track], and the lyric track a melody is sung with, which its save
 * button applies.
 */
context(model: MusicEditor)
private fun ViewScope.trackEditPopover(id: Id, track: Track) {
    popover(id) { popover ->
        val nameState = storeOf(track.name)
        val capoTextState = storeOf(track.capo?.toString().orEmpty())
        val lyricTracks = model.lyricTracksState.now
        val lyricNames = listOf(FIRST_LYRICS) + lyricTracks.map { it.name }
        val lyricsState = storeOf((track as? MelodyTrack)?.lyricsId?.let { id -> lyricTracks.firstOrNull { it.trackId == id }?.name } ?: FIRST_LYRICS)
        val save = save@{
            val capo = readCapoText(capoTextState.now) ?: return@save toaster.deliver(MusicFormProblem.capo)
            val lyrics = lyricTracks.firstOrNull { it.name == lyricsState.now }?.trackId
            model.editTrack(track.trackId, nameState.now, capo.value, lyrics)
            popover.close()
        }
        column(Padding(2)) {
            row(AlignItemsCenter) {
                textField(nameState, "name", Flex1, onEnterSubmit = save)
                textField(capoTextState, "capo", size = 4)
            }
            if (track is MelodyTrack) dropMenu(lyricNames, lyricsState)
            button("Save", save)
        }
    }
}

/**
 * Renders the arrangement every track follows under the music's properties, with actions to add a section to it and to
 * edit the properties: a row for each section it plays, in order, with its measures and actions that edit the section
 * and take it out of the arrangement.
 */
context(model: MusicEditor)
private fun ViewScope.musicSection() {
    val editId = Id(Uuid.random().toString())
    val addId = Id(Uuid.random().toString())
    musicEditPopover(editId)
    arrangementAddPopover(addId)
    val add = IconAction(SvgFile.Plus.small, "Add", config = { setPopoverTarget(addId, true) }) { }
    val edit = IconAction(SvgFile.Gear.small, "Edit", config = { setPopoverTarget(editId, true) }) { }
    actionSection("Arrangement", level = HeadingLevel.H4, icons = listOf(add, edit)) {
        flowBlock(model.musicState) { music -> textBlock(music.summary, modify(TextSmall, InkDimFg)) }
        flowBlock(model.arrangementState, FlexColumn) { sections ->
            val sectionIds = sections.map { it.sectionId }
            sections.forEachIndexed { index, section ->
                val sectionEditId = Id(Uuid.random().toString())
                sectionEditPopover(sectionEditId, section)
                val sectionEdit = IconAction(SvgFile.Gear.small, "Edit", config = { setPopoverTarget(sectionEditId, true) }) { }
                val remove = IconAction(SvgFile.Trash.small, "Remove") {
                    model.setMusicArrangement(sectionIds.filterIndexed { at, _ -> at != index })
                }
                actionRow("${section.name} · ${section.measures}", icons = listOf(sectionEdit, remove))
            }
            if (sections.isEmpty()) textBlock("The arrangement plays no sections yet.", modify(TextSmall, InkDimFg))
        }
    }
}

/**
 * A popover with [id] holding the sections of the music, where a click adds one to the end of the arrangement, above
 * the fields of a new section's name and measures.
 */
context(model: MusicEditor)
private fun ViewScope.arrangementAddPopover(id: Id) {
    popover(id) { popover ->
        flowBlock(model.sectionsState) { sections ->
            actionMenu(sections.map { section ->
                MenuAction(section.name) {
                    popover.close()
                    model.setMusicArrangement(model.musicState.now.arrangement + section.sectionId)
                }
            })
        }
        sectionFields("Add") { name, measures ->
            model.addSection(name, measures).also { if (it) popover.close() }
        }
    }
}

/** A popover with [id] holding the name and measures of [section], which its save button applies in every track. */
context(model: MusicEditor)
private fun ViewScope.sectionEditPopover(id: Id, section: Section) {
    popover(id) { popover ->
        sectionFields("Save", section) { name, measures ->
            model.editSection(section.sectionId, name, measures)
            popover.close()
            true
        }
    }
}

/**
 * Renders the fields of a section's name and measures, seeded from [section], and a button labeled [label] that hands
 * them to [onSubmit], which returns whether it took them. Measures below one are refused.
 */
private fun ViewScope.sectionFields(label: String, section: Section? = null, onSubmit: (String, Int) -> Boolean) {
    val nameState = storeOf(section?.name.orEmpty())
    val measuresState = storeOf(section?.measures?.toString().orEmpty())
    val submit = submit@{
        val measures = measuresState.now.trim().toIntOrNull()?.takeIf { it >= 1 } ?: return@submit toaster.deliver(MusicFormProblem.measures)
        if (onSubmit(nameState.now, measures)) {
            nameState.set(section?.name.orEmpty())
            measuresState.set(section?.measures?.toString().orEmpty())
        }
    }
    row(modify(AlignItemsCenter, Padding(2))) {
        textField(nameState, "name", Flex1, onEnterSubmit = submit)
        textField(measuresState, "measures", size = 4, onEnterSubmit = submit)
        button(label, submit)
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
                model.setMusic(music)
                popover.close()
            })
        }
    }
}

/**
 * Renders [picks] in order, each a button labeled by [labelOf] that takes it out, then a button that opens a menu of
 * [choices] to add one. Each change is handed to [onChange].
 */
fun <T> ViewScope.namedPicks(
    picks: List<T>,
    choices: List<T>,
    labelOf: (T) -> String,
    onChange: (List<T>) -> Unit,
) {
    val menuId = Id(Uuid.random().toString())
    popover(menuId) { popover ->
        actionMenu(choices.map { choice ->
            MenuAction(labelOf(choice)) {
                popover.close()
                onChange(picks + choice)
            }
        })
    }
    row(modify(AlignItemsCenter, FlexWrap, Flex1)) {
        picks.forEachIndexed { index, pick ->
            button("${labelOf(pick)} ×", { onChange(picks.filterIndexed { at, _ -> at != index }) }, TextSmall)
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

/** The strips of [track] in [source], a line of its text to each, with its lyrics under each while [config] shows them. */
context(config: MusicConfig)
fun ViewScope.notationStrips(source: MusicSource, track: TrackSource) {
    val lines = track.layout.lines
    // the pickup and staff hang off the left of each strip, so a margin as wide as the wider keeps them in the pane
    val leadTicks = maxOf(source.pickup.ticks, NoteValue.Sixteenth.ticks).toDouble()
    val paneTicks = (lines.maxOfOrNull { source.downbeatTicksOf(it) } ?: 0) + leadTicks
    // a line of few measures would stretch across a wide pane, so a beat takes no more than its widest
    val maxWidth = LinearDimension("calc(var(--unit) * ${paneTicks / Beats.TICKS_PER_BEAT * MAX_BEAT_UNITS})")
    // the first head and its sharp reach left of their onset by a fixed width, which no margin of ticks covers
    val headRoom = LinearDimension("calc(var(--unit) * $HEAD_ROOM_UNITS)")
    column(modify(Css.MaxWidth.of(maxWidth), Css.Gap.of(2.px))) {
        lines.forEach { line ->
            val place = modify(
                Css.MarginLeft.of((leadTicks / paneTicks * 100).pct),
                Css.Width.of((source.downbeatTicksOf(line) / paneTicks * 100).pct),
            )
            // each line on a band of its own, the band reaching the whole width however short the line
            div(modify(ZenBg, Css.PaddingLeft.of(headRoom))) {
                notationMeasures(source, track, line, place)
            }
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

/** The name of the lyric choice that sings a melody with the music's first lyric track. */
private const val FIRST_LYRICS = "First lyrics"