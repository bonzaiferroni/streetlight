package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import streetlight.web.utility.downloadFile
import streetlight.model.music.MusicSheet

/**
 * Hands the viewer [sheet] as an Ogg Opus file, rendered by [player] with [takes] in place of their sections' tones,
 * or gives the problem. This is the deliberate second encode of any take it holds.
 */
suspend fun exportOgg(player: TrackPlayer, sheet: MusicSheet, takes: SongTakes?): Outcome<Unit> {
    val pcm = player.render(sheet, takes) ?: return Problem("The song has nothing to play yet")
    val bytes = encodeOggOpus(pcm).toDataOr { return it }
    downloadFile(bytes, "${sheet.music.name}.opus", OGG_TYPE)
    return Ok(Unit)
}

private const val OGG_TYPE = "audio/ogg"
