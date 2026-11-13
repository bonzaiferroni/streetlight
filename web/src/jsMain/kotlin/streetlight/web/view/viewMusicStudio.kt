package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.launchEffect
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.itemsBlock
import koala.dom.flowModifier
import kampfire.model.tapOf
import koala.dom.layout.lazyTabs
import koala.dom.routeBlock
import koala.model.IconAction
import koala.model.MenuAction
import koala.model.NullContent
import koala.modifier.*
import koala.page.Id
import streetlight.model.music.MusicId
import streetlight.model.ui.MusicStudioRoute
import streetlight.model.ui.MusicStudioTab
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.page.applyTheme
import streetlight.web.dom.page.dimLights
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.mainBody
import streetlight.web.dom.layout.topSection
import streetlight.web.model.LaneEditor
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import streetlight.web.model.exportOgg
import streetlight.web.model.MixEditor
import koala.component.heading6
import streetlight.web.model.getMusicStudio
import streetlight.web.model.portal
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid
import kampfire.model.toDataOr
import koala.dom.layout.transTabs
import kotlinx.coroutines.launch
import streetlight.web.model.toaster
import streetlight.web.utility.pickFile
import web.blob.text

/** Renders the music studio: the music the route names, or the kept musics when it names none that is kept. */
context(route: MusicStudioRoute)
fun ViewScope.viewMusicStudio() {
    val model = app.getMusicStudio(contentScope, route.musicId)
    val player = app.get<TrackPlayer>()
    // the studio's colors carry meaning, which the page's lights would tint
    dimLights()
    onDispose { applyTheme(null) }
    // a change of music keeps the view, so the routes that follow reach it here
    launchEffect("viewMusicStudio > route") {
        portal.routeFlowOf<MusicStudioRoute>().collect { next ->
            model.setMusicId(next.musicId)
            document.setTitle(next)
        }
    }
    context(model, player) {
        mainBody("Music", "Studio", "viewMusicStudio.kt", MusicStudioRoute()) {
            flowBlock(model.sheetState, modify(Transitioner)) { sheet ->
                if (sheet == null) musicList() else musicFocus(model.editorOf(contentScope, sheet))
            }
        }
    }
}

/** The view transition name of the name of the music [musicId], shared by its row and its heading. */
private fun musicNameTransition(musicId: MusicId) = transition("music-$musicId")

fun RouteScope.viewMusicStudioRoute() {
    routeBlock<MusicStudioRoute, NullContent> {
        viewMusicStudio()
    }
}

/**
 * Renders the music of [editor] in a section headed by its name, with a way back to the musics and a gear that opens
 * how its notation is written and shown and the formats it exports to, as a tab for each layout.
 */
context(model: MusicStudio, player: TrackPlayer)
private fun ViewScope.musicFocus(editor: MusicEditor) {
    val mix = model.mixEditorOf(contentScope, editor)
    val lanes = LaneEditor(contentScope, editor, mix, player, app.get())
    // playback left running when the music closes would keep sounding
    onDispose { player.stop() }
    val settingsId = Id(Uuid.random().toString())
    songSettingsPopover(settingsId, editor, mix)
    topSection(
        label = editor.musicState.now.name,
        onBack = { portal.go(MusicStudioRoute()) },
        headingMod = musicNameTransition(editor.musicState.now.musicId),
        icon = IconAction(SvgFile.Gear.large, "Settings", config = { setPopoverTarget(settingsId, true) }) { },
    ) {
        transTabs(paramKey = MusicStudioRoute.tabParam.key) {
            MusicStudioTab.entries.forEach { studioTab ->
                tab(studioTab.label, studioTab.svg) {
                    when (studioTab) {
                        MusicStudioTab.Compose -> context(editor, mix) { musicEditor() }
                        MusicStudioTab.Mix -> context(mix) { mixEditor() }
                        MusicStudioTab.Timeline -> context(lanes) { laneEditor() }
                    }
                }
            }
        }
    }
}

/**
 * A popover with [id] holding the style notation is written in, the view it is shown in and whether lyrics show, above
 * the formats the music of [editor] exports to, with the takes of [mix].
 */
context(model: MusicStudio, player: TrackPlayer)
private fun ViewScope.songSettingsPopover(id: Id, editor: MusicEditor, mix: MixEditor) {
    popover(id) { popover ->
        column(Padding(2)) {
            dropMenu(model.notationStyleState)
            dropMenu(model.notationViewState)
            switch("Lyrics", model.isLyricsShownState)
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
                        exportOgg(player, editor.sheetState.now, mix.readTakes()).toDataOr(toaster) { return@launch }
                    }
                },
            ))
        }
    }
}

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
        itemsBlock(model.songMusicsState, Transitioner, gapRems = null) { songMusic ->
            val musicId = songMusic.music.musicId
            actionRow(
                label = songMusic.music.name,
                actions = listOf(
                    MenuAction("Edit") { portal.go(MusicStudioRoute(musicId)) },
                    MenuAction("Remove") { model.remove(songMusic) },
                ),
                labelMod = musicNameTransition(musicId),
            )
        }
        textBlock("No songs yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            .flowModifier(model.songMusicsState.tapOf { it.isNotEmpty() }, DisplayNone, contentScope)
    }
}

/** Asks the viewer for a markdown file, stores the song it holds and opens its music, or toasts its problem. */
context(model: MusicStudio)
private fun ViewScope.importSong() = pickFile(MARKDOWN_ACCEPT) { file ->
    contentScope.launch {
        val musicId = model.importSong(file.text(), file.name).toDataOr(toaster) { return@launch }
        portal.go(MusicStudioRoute(musicId))
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
            contentScope.launch { portal.go(MusicStudioRoute(model.create(title))) }
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(titleState, "title", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

private const val MARKDOWN_ACCEPT = ".md,text/markdown"
