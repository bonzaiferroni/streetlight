package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.component.heading3
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.routeBlock
import koala.model.MenuAction
import koala.model.NullContent
import koala.modifier.*
import koala.page.Id
import streetlight.model.data.Chromatic
import streetlight.model.ui.SongConfigRoute
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.MusicEditor
import streetlight.web.model.getSongEditor
import streetlight.web.utility.setTitle
import web.dom.document
import kotlin.uuid.Uuid

/** Renders the music editor, beside the stored songs it loads. */
fun ViewScope.viewSongConfig() {
    val model = app.getSongEditor(contentScope)
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
            flowBlock(model.musicState) { music ->
                if (music == null) return@flowBlock
                textBlock("Key: ${Chromatic.ofPitch(music.key.tonic.value).label} ${music.key.mode}")
                textBlock("Time: ${music.time.beats}/${music.time.beatUnit}")
            }
        }
    }
}
