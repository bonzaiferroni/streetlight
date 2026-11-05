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
import streetlight.web.model.NotationEditor
import streetlight.web.model.chordEditorOf
import streetlight.web.model.lyricEditorOf
import streetlight.web.model.melodyEditorOf
import streetlight.web.model.musicTextEditorOf
import streetlight.web.model.MusicEditor
import streetlight.web.model.label
import streetlight.web.model.getMusicEditor
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid

/** Renders the music editor, beside the stored songs it loads. */
fun ViewScope.viewSongConfig() {
    val model = app.getMusicEditor(contentScope)
    context(model) {
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

context(model: MusicEditor)
private fun ViewScope.songList() {
    val newId = Id(Uuid.random().toString())
    newSongPopover(newId)
    actionSection(
        label = "Songs",
        actions = listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { }),
    ) {
        flowBlock(model.songsState) { songs ->
            songs.forEach { entry ->
                val songId = entry.song.songId
                actionRow(
                    label = entry.song.title,
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
context(model: MusicEditor)
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

/** Renders the loaded song's music and the style it is written in, or nothing while no song is loaded. */
context(model: MusicEditor)
private fun ViewScope.musicEditor() {
    flowBlock(model.songState) { song ->
        if (song == null) return@flowBlock
        column {
            row(AlignItemsCenter) {
                heading3(song.title, Flex1)
                dropMenu(model.notationStyleState)
            }
            notationPanes("music", musicTextEditorOf(contentScope, model), rows = 3)
            trackList()
        }
    }
}

context(model: MusicEditor)
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
        ))
    }
    actionSection(
        label = "Tracks",
        actions = listOf(
            MenuAction("Play") { model.playSong() },
            MenuAction("Stop") { model.stop() },
            MenuAction("Export") { model.exportMidi() },
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
context(model: MusicEditor)
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
context(model: MusicEditor)
private fun ViewScope.trackEditor(index: Int) {
    val track = model.readTrack(index) ?: return
    column {
        when (track) {
            is ChordTrack -> {
                trackPlayer(index)
                notationPanes("chords", chordEditorOf(contentScope, model, index))
            }
            is MelodyTrack -> {
                trackPlayer(index)
                notationPanes("jianpu", melodyEditorOf(contentScope, model, index))
            }
            is LyricTrack -> notationPanes("lyrics", lyricEditorOf(contentScope, model, index))
            is DrumTrack -> textBlock("Hello ${track.label}")
        }
        flowBlock(model.sectionWarningsState(index)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/** Renders the button that plays the track at [index], or stops it while it plays. */
context(model: MusicEditor)
private fun ViewScope.trackPlayer(index: Int) = flowBlock(model.isPlayingState) { isPlaying ->
    if (isPlaying) {
        button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    } else {
        button(SvgFile.PlayerPlay.filled, { model.playTrack(index) }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    }
}

/** Renders the text [editor] reads beside the text its track writes back, so the two can be compared. */
private fun ViewScope.notationPanes(label: String, editor: NotationEditor<*>, rows: Int = 16) {
    row(AlignItemsStart) {
        column(Flex1) {
            textEditor(editor.textState, label, rows = rows, textMod = MonoFont)
            flowBlock(editor.errorsState) { errors ->
                errors.forEach { textBlock("Line ${it.line}: ${it.message}", modify(TextSmall, InkDimFg)) }
            }
        }
        flowBlock(editor.notationState, Flex1) { notation ->
            textBlock(notation.orEmpty(), modify(MonoFont, WhiteSpacePreLine))
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
