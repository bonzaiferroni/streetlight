package streetlight.web.model

import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.SongMarkdown
import streetlight.web.utility.downloadFile

/** Hands the viewer [song] as a markdown file, chords spelled in [style]. */
fun exportMarkdown(song: StoredSong, style: NotationStyle) {
    val text = SongMarkdown.write(song.song, song.music, song.tracks, style)
    downloadFile(text.encodeToByteArray(), "${song.song.title}.md", MARKDOWN_TYPE)
}

private const val MARKDOWN_TYPE = "text/markdown"
