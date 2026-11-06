package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.component.IconStyle
import koala.SvgPack
import koala.component.heading3
import koala.component.heading5
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.routeBlock
import koala.model.MenuAction
import koala.model.NullContent
import koala.modifier.*
import koala.page.Id
import kampfire.model.mutableTapOf
import streetlight.model.data.ChordTrack
import streetlight.model.data.MelodyTrack
import streetlight.model.data.DrumTrack
import streetlight.model.data.LyricTrack
import streetlight.model.data.Track
import streetlight.model.ui.SongConfigRoute
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.ChordEditor
import streetlight.web.model.DrumEditor
import streetlight.web.model.LyricEditor
import streetlight.web.model.MelodyEditor
import kampfire.model.MutableTap
import kampfire.model.Tap
import streetlight.model.data.NotationError
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import streetlight.web.model.label
import streetlight.web.model.getMusicStudio
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid

/** Renders the music studio: the stored songs and the editor of the one open. */
fun ViewScope.viewSongConfig() {
    val model = app.getMusicStudio(contentScope)
    val player = app.get<TrackPlayer>()
    context(model, player) {
        mainBody("viewSongConfig.kt") {
            column {
                songList()
                musicEditor()
            }
        }
    }

    document.setTitle(SongConfigRoute)
}

fun RouteScope.viewSongConfigRoute() {
    routeBlock<SongConfigRoute, NullContent> {
        viewSongConfig()
    }
}

context(model: MusicStudio)
private fun ViewScope.songList() {
    val newId = Id(Uuid.random().toString())
    newSongPopover(newId)
    actionSection(
        label = "Songs",
        actions = listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { }),
    ) {
        flowBlock(model.songList) { songs ->
            songs.forEach { song ->
                val songId = song.songId
                actionRow(
                    label = song.title,
                    actions = listOf(
                        MenuAction("Edit") { model.load(songId) },
                        MenuAction("Remove") { model.remove(songId) },
                    ),
                )
            }
            if (songs.isEmpty()) {
                textBlock("No songs yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            }
        }
    }
}

/** A popover with [id] holding a field for a new song's title. */
context(model: MusicStudio)
private fun ViewScope.newSongPopover(id: Id) {
    popover(id) { popover ->
        val titleState = storeOf("")
        val save = save@{
            val title = titleState.now.takeIf { it.isNotBlank() }?.trim() ?: return@save
            model.create(title)
            titleState.set("")
            popover.close()
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(titleState, "title", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

/** Renders the open song's music and the style it is written in, or nothing while no song is open. */
context(model: MusicStudio, player: TrackPlayer)
private fun ViewScope.musicEditor() {
    flowBlock(model.editorState) { editor ->
        if (editor == null) return@flowBlock
        // playback left running when the song closes would keep sounding
        onDispose { player.stop() }
        column {
            row(AlignItemsCenter) {
                heading3(editor.song.title, Flex1)
                dropMenu(model.notationStyleState)
            }
            with(editor.properties) { notationPanes("music", textState, errorsState, notationState, rows = 4) }
            context(editor) { trackList() }
        }
    }
}

context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackList() {
    val newId = Id(Uuid.random().toString())
    popover(newId) { popover ->
        actionMenu(listOf(
            MenuAction("Chords") {
                model.addTrack(ChordTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Melody") {
                model.addTrack(MelodyTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Lyrics") {
                model.addTrack(LyricTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Drums") {
                model.addTrack(DrumTrack(emptyList(), emptyList()))
                popover.close()
            },
        ))
    }
    actionSection(
        label = "Tracks",
        actions = listOf(
            MenuAction("Play") { player.play(model.storedState.now) },
            MenuAction("Stop") { player.stop() },
            MenuAction("Export") { exportMidi(model.storedState.now) },
            MenuAction("New", config = { setPopoverTarget(newId, true) }) { },
        ),
    ) {
        // the list follows the labels alone, so an edit to a track's content leaves its open editor in place
        flowBlock(model.trackLabelsState) { labels ->
            labels.forEachIndexed { index, label ->
                model.readTrack(index)?.let { trackRow(index, label, it.svg) }
            }
            if (labels.isEmpty()) {
                textBlock("No tracks yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            }
        }
    }
}

/** Renders the row of the track at [index] shown as [svg] and [label], which opens its editor below it or closes it. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackRow(index: Int, label: String, svg: SvgPack) {
    val nameId = Id(Uuid.random().toString())
    trackNamePopover(nameId, index, label)
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        button({ model.toggleTrack(index) }, Flex1) {
            row(AlignItemsCenter) {
                icon(svg.large, Height(5))
                heading5(label)
            }
        }
        menuActionsRow(listOf(
            MenuAction("Edit", config = { setPopoverTarget(nameId, true) }) { },
            MenuAction("Remove") { model.removeTrack(index) },
        ))
    }
    flowBlock(model.isTrackOpenState(index)) { isOpen ->
        if (isOpen) trackEditor(index)
    }
}

/** A popover with [id] holding a field for the name of the track at [index], starting from [label]. */
context(model: MusicEditor)
private fun ViewScope.trackNamePopover(id: Id, index: Int, label: String) {
    popover(id) { popover ->
        val nameState = storeOf(model.readTrack(index)?.name ?: label)
        val save = {
            model.renameTrack(index, nameState.now)
            popover.close()
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(nameState, "name", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

/** Renders the editor of the track at [index]. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackEditor(index: Int) {
    val track = model.readTrack(index) ?: return
    column {
        when (track) {
            is ChordTrack -> {
                trackPlayer(index)
                with(ChordEditor(contentScope, model, index)) {
                    notationPanes("chords", textState, errorsState, notationState)
                }
            }
            is MelodyTrack -> {
                trackPlayer(index)
                with(MelodyEditor(contentScope, model, index)) {
                    notationPanes("jianpu", textState, errorsState, notationState)
                }
            }
            is LyricTrack -> with(LyricEditor(contentScope, model, index)) {
                notationPanes("lyrics", textState, errorsState, notationState)
            }
            is DrumTrack -> with(DrumEditor(contentScope, model, index)) {
                notationPanes("drums", textState, errorsState, notationState)
            }
        }
        flowBlock(model.sectionWarningsState(index)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/** Renders the button that plays the track at [index], or stops it while it plays. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackPlayer(index: Int) = flowBlock(player.isPlayingState) { isPlaying ->
    if (isPlaying) {
        button(SvgFile.PlayerStop.filled, { player.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    } else {
        button(
            SvgFile.PlayerPlay.filled,
            { model.readTrack(index)?.let { player.play(model.storedState.now, it) } },
            modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")),
        )
    }
}

/**
 * Renders the text in [textState] and its [errorsState] beside the text written back in [notationState], so the two
 * can be compared.
 */
private fun ViewScope.notationPanes(
    label: String,
    textState: MutableTap<String>,
    errorsState: Tap<List<NotationError>>,
    notationState: Tap<String>,
    rows: Int = 16,
) {
    row(AlignItemsStart) {
        column(Flex1) {
            textEditor(textState, label, rows = rows, textMod = MonoFont)
            flowBlock(errorsState) { errors ->
                errors.forEach { textBlock("Line ${it.line}: ${it.message}", modify(TextSmall, InkDimFg)) }
            }
        }
        flowBlock(notationState, Flex1) { notation ->
            textBlock(notation, modify(MonoFont, WhiteSpacePreLine))
        }
    }
}

/** The icon of the track's type. */
private val Track.svg get() = when (this) {
    is ChordTrack -> SvgFile.AcousticGuitar
    is MelodyTrack -> SvgFile.Music
    is LyricTrack -> SvgFile.Microphone
    is DrumTrack -> SvgFile.Drum
}
