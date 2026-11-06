package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.lazyTabs
import koala.dom.routeBlock
import koala.model.MenuAction
import koala.model.NullContent
import koala.modifier.*
import koala.page.Id
import streetlight.model.ui.MusicStudioRoute
import streetlight.model.ui.MusicStudioTab
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.LaneEditor
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.TrackPlayer
import streetlight.web.model.getMusicStudio
import streetlight.web.model.portal
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid

/** Renders the music studio: the song the route names, or the stored songs when it names none that is stored. */
context(route: MusicStudioRoute)
fun ViewScope.viewMusicStudio() {
    val model = app.getMusicStudio(contentScope)
    val player = app.get<TrackPlayer>()
    context(model, player) {
        mainBody("viewMusicStudio.kt") {
            val editor = route.songId?.let { model.editorOf(contentScope, it) }
            if (editor == null) songList() else songFocus(editor)
        }
    }

    document.setTitle(route)
}

fun RouteScope.viewMusicStudioRoute() {
    routeBlock<MusicStudioRoute, NullContent> {
        viewMusicStudio()
    }
}

/** Renders the song of [editor] in a section headed by its title, with a way back to the songs, as a tab for each layout. */
context(model: MusicStudio, player: TrackPlayer)
private fun ViewScope.songFocus(editor: MusicEditor) {
    val lanes = LaneEditor(contentScope, editor, player)
    // playback left running when the song closes would keep sounding
    onDispose { player.stop() }
    actionSection(
        label = editor.song.title,
        actions = listOf(MenuAction("Back") { portal.go(MusicStudioRoute()) }),
    ) {
        lazyTabs(paramKey = MusicStudioRoute.tabParam.key) {
            MusicStudioTab.entries.forEach { studioTab ->
                tab(studioTab.label, studioTab.svg) {
                    when (studioTab) {
                        MusicStudioTab.Arrangement -> context(editor) { musicEditor() }
                        MusicStudioTab.Lanes -> context(lanes) { laneEditor() }
                    }
                }
            }
        }
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
                        MenuAction("Edit") { portal.go(MusicStudioRoute(songId)) },
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
            val songId = model.create(title)
            titleState.set("")
            popover.close()
            portal.go(MusicStudioRoute(songId))
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(titleState, "title", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}
