package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import streetlight.web.utility.downloadFile
import streetlight.model.music.MusicSheet
import streetlight.model.music.TrackClip

/**
 * Hands the viewer [sheet] as an Ogg Opus file, rendered by [player] with [musicWaves] in place of tones,
 * or gives the problem. This is the deliberate second encode of any take it holds.
 */
suspend fun exportOgg(player: TrackPlayer, sheet: MusicSheet, musicWaves: Map<TrackClip, MusicWave>): Outcome<Unit> {
    val wave = player.render(sheet, musicWaves) ?: return Problem("The song has nothing to play yet")
    val bytes = encodeOggOpus(wave).toDataOr { return it }
    downloadFile(bytes, "${sheet.music.name}.opus", OGG_TYPE)
    return Ok(Unit)
}

private const val OGG_TYPE = "audio/ogg"
