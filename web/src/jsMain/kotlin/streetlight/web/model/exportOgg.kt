package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import streetlight.web.utility.downloadFile

/**
 * Hands the viewer [song] as an Ogg Opus file, rendered by [player] with [takes] in place of their sections' tones,
 * or gives the problem. This is the deliberate second encode of any take it holds.
 */
suspend fun exportOgg(player: TrackPlayer, song: StoredSong, takes: SongTakes?): Outcome<Unit> {
    val pcm = player.render(song, takes) ?: return Problem("The song has nothing to play yet")
    val bytes = encodeOggOpus(pcm).toDataOr { return it }
    downloadFile(bytes, "${song.song.title}.opus", OGG_TYPE)
    return Ok(Unit)
}

private const val OGG_TYPE = "audio/ogg"
