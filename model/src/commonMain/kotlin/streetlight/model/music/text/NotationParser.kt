package streetlight.model.music.text

import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackType
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.trackOf
import streetlight.model.music.ui.lyricsOf

/**
 * Reads [text], the measures of [notation] alone, in the notation of its track type against [music]. The notation read
 * keeps its id and name, and a stanza is checked against [phrase], the melody sung with it.
 */
fun MusicSheet.notationParseOf(
    notationId: NotationId,
    trackType: TrackType,
    name: String?,
    text: String,
): NotationParse<Notation> = when (trackType) {
    TrackType.Chords -> ChordParser(music.key).readNotation(notationId, name, text, music.time)
    TrackType.Melody -> MelodyParser(music.key).readNotation(notationId, name, text, music.time)
    TrackType.Lyrics -> LyricParser.readNotation(notationId, name, text, music.time, getMelody(notationId))
    TrackType.Drums -> DrumParser.readNotation(notationId, name, text, music.time)
}

/** Writes [notation] as the text */
fun notationTextOf(
    notation: Notation,
    key: Key,
    time: TimeSignature,
): String = when (notation) {
    is Progression -> ChordParser(key).writeNotation(notation, time)
    is Phrase -> MelodyParser(key).writeNotation(notation, time)
    is Stanza -> LyricParser.writeNotation(notation, time)
    is Groove -> DrumParser.writeNotation(notation, time)
}

private fun MusicSheet.getMelody(notationId: NotationId): Phrase? {
    val track = trackOf(notationId)
    val sectionId = track.sectionIdsOf(notationId).firstOrNull() ?: return null
    val melody = tracks.filterIsInstance<MelodyTrack>().firstOrNull { tracks.lyricsOf(it)?.trackId == track.trackId }
        ?: return null
    val phraseId = melody.sections[sectionId] ?: return null
    return melody.notations.firstOrNull { it.notationId == phraseId }
}