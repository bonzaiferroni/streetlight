package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.itemsBlock
import koala.dom.flowModifier
import kampfire.model.tapOf
import koala.dom.routeBlock
import koala.model.IconAction
import koala.model.MenuAction
import koala.model.NullContent
import koala.modifier.*
import koala.page.Id
import streetlight.model.music.MusicId
import streetlight.model.ui.MusicEditorRoute
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.model.ui.MusicStudioRoute
import streetlight.model.ui.MusicStudyRoute
import streetlight.model.ui.MusicStudioTab
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.mainBody
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.MusicStudioFocus
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import streetlight.web.model.exportOgg
import koala.component.heading6
import streetlight.web.model.getMusicStudio
import streetlight.web.model.portal
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid
import kampfire.model.toDataOr
import koala.dom.layout.lazyTabs
import koala.dom.layout.lazyTransTabs
import koala.dom.layout.transTabs
import kotlinx.coroutines.launch
import streetlight.model.music.MusicSheet
import streetlight.model.studio.StudioDb
import streetlight.web.model.LaneEditor
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicHost
import streetlight.web.model.WaveShelf
import streetlight.web.model.toaster
import streetlight.web.utility.pickFile
import web.blob.text

/** Renders the music studio: the music the route names, or the kept musics when it names none that is kept. */
context(route: MusicStudioRoute)
fun ViewScope.musicStudioScreen() {
    val model = app.getMusicStudio(contentScope, route)
    val config = MusicConfig(contentScope)
    val player = app.get<TrackPlayer>()
    val db = app.get<StudioDb>()
    val waveShelf = app.get<WaveShelf>()
    // the studio's colors carry meaning, which the page's lights would tint
    // dimLights()
    // onDispose { applyTheme(null) }
    // a change of music keeps the view, so the routes that follow reach it here
    launchEffect("musicStudioScreen > route") {
        portal.routeFlowOf<MusicStudioRoute>().collect { next ->
            model.setFocus(next)
            document.setTitle(next)
        }
    }
    context(model, player, config, db, waveShelf) {
        mainBody("Music", "Studio", "musicStudioScreen.kt", MusicStudioIndexRoute) {
            flowBlock(model.focusState, modify(Transitioner)) { focus ->
                when (focus) {
                    null -> column {
                        studyList()
                        musicList()
                    }
                    is MusicStudioFocus.Music -> musicFocus(focus.sheet)
                    is MusicStudioFocus.Study -> studyFocus(focus.musicStudy)
                }
            }
        }
    }
}

fun RouteScope.viewMusicStudioRoute() {
    routeBlock<MusicStudioRoute, NullContent> {
        musicStudioScreen()
    }
}

/**
 * A popover with [id] holding the style notation is written in, the view it is shown in and whether lyrics show, above
 * the formats the music of [editor] exports to, with its bound clips.
 */
context(model: MusicStudio, player: TrackPlayer, config: MusicConfig, waveShelf: WaveShelf)
private fun ViewScope.songSettingsPopover(id: Id, editor: MusicEditor) {
    popover(id) { popover ->
        column(Padding(2)) {
            dropMenu(config.notationStyleState)
            dropMenu(config.notationViewState)
            switch("Lyrics", config.isLyricsShownState)
            filigree { heading6("export") }
            actionMenu(listOf(
                MenuAction("Markdown") {
                    popover.close()
                    contentScope.launch { model.exportMarkdown(editor.sheetState.now) }
                },
                MenuAction("MIDI") {
                    exportMidi(editor.sheetState.now)
                    popover.close()
                },
                MenuAction("Ogg") {
                    popover.close()
                    contentScope.launch {
                        val musicWaves = waveShelf.musicWavesOf(editor.sheetState.now)
                        exportOgg(player, editor.sheetState.now, musicWaves).toDataOr(toaster) { return@launch }
                    }
                },
            ))
        }
    }
}

/** The view transition name of the name of the music [musicId], shared by its row and its heading. */
fun musicNameTransition(musicId: MusicId) = transition("music-$musicId")

context(model: MusicStudio)
private fun ViewScope.musicList() {
    val newId = Id(Uuid.random().toString())
    newSongPopover(newId)
    actionSection(
        label = "Songs",
        actions = listOf(
            MenuAction("Import") { importSong() },
            MenuAction("New", config = { setPopoverTarget(newId, true) }) { },
        ),
    ) {
        itemsBlock(model.songsState, Transitioner, gapRems = null) { songMusic ->
            val musicId = songMusic.music.musicId
            actionRow(
                label = songMusic.music.name,
                route = MusicEditorRoute(musicId),
                actions = listOf(
                    MenuAction("Remove") { model.remove(songMusic) },
                ),
                labelMod = musicNameTransition(musicId),
            )
        }
        textBlock("No songs yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            .flowModifier(model.songsState.tapOf { it.isNotEmpty() }, DisplayNone, contentScope)
    }
}

context(model: MusicStudio)
private fun ViewScope.studyList() {
    actionSection(label = "Studies") {
        itemsBlock(model.studiesState, Transitioner, gapRems = null) { study ->
            actionRow(label = study.label, route = MusicStudyRoute(study.study.trackId, study.study.notationId))
        }
    }
}

/** Asks the viewer for a markdown file, stores the song it holds and opens its music, or toasts its problem. */
context(model: MusicStudio)
private fun ViewScope.importSong() = pickFile(MARKDOWN_ACCEPT) { file ->
    contentScope.launch {
        val musicId = model.importSong(file.text(), file.name).toDataOr(toaster) { return@launch }
        portal.go(MusicEditorRoute(musicId))
    }
}

/** A popover with [id] holding a field for a new song's title. */
context(model: MusicStudio)
private fun ViewScope.newSongPopover(id: Id) {
    popover(id) { popover ->
        val titleState = storeOf("")
        val save = save@{
            val title = titleState.now.takeIf { it.isNotBlank() }?.trim() ?: return@save
            titleState.set("")
            popover.close()
            contentScope.launch { portal.go(MusicEditorRoute(model.create(title))) }
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(titleState, "title", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

private const val MARKDOWN_ACCEPT = ".md,text/markdown"
