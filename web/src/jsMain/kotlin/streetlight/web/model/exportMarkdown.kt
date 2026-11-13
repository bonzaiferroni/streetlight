package streetlight.web.model

import streetlight.model.music.MusicSheet
import streetlight.model.music.Song
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.SongMarkdown
import streetlight.web.utility.downloadFile

/** Hands the viewer [sheet] under the title and artist of [song] as a markdown file, chords spelled in [style]. */
fun exportMarkdown(song: Song, sheet: MusicSheet, style: NotationStyle) {
    val text = SongMarkdown.write(song, sheet.music, sheet.tracks, style)
    downloadFile(text.encodeToByteArray(), "${sheet.music.name}.md", MARKDOWN_TYPE)
}

private const val MARKDOWN_TYPE = "text/markdown"
