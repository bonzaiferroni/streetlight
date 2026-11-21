package streetlight.model.music.text

import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.Music
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackType

/**
 * Reads [text], the measures of [notation] alone, in the notation of its track type against [music]. The notation read
 * keeps its id and name, and a stanza is checked against [phrase], the melody sung with it.
 */
fun notationParseOf(
    notationId: NotationId,
    trackType: TrackType,
    name: String?,
    text: String,
    key: Key,
    time: TimeSignature,
    phrase: Phrase? = null,
): NotationParse<Notation> = when (trackType) {
    TrackType.Chords -> ChordParser(key).readNotation(notationId, name, text, time)
    TrackType.Melody -> MelodyParser(key).readNotation(notationId, name, text, time)
    TrackType.Lyrics -> LyricParser.readNotation(notationId, name, text, time, phrase)
    TrackType.Drums -> DrumParser.readNotation(notationId, name, text, time)
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
