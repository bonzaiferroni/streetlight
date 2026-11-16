package streetlight.model.music.text

import streetlight.model.music.Groove
import streetlight.model.music.Music
import streetlight.model.music.Notation
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza

/**
 * Reads [text], the measures of [notation] alone, in the notation of its track type against [music]. The notation read
 * keeps its id and name, and a stanza is checked against [phrase], the melody sung with it.
 */
fun readNotation(
    notation: Notation,
    text: String,
    music: Music,
    phrase: Phrase? = null,
): NotationRead<Notation> = when (notation) {
    is Progression -> ChordParser(music.key).readNotation(notation.notationId, notation.name, text, music.time)
    is Phrase -> MelodyParser(music.key).readNotation(notation.notationId, notation.name, text, music.time)
    is Stanza -> LyricParser.readNotation(notation.notationId, notation.name, text, music.time, phrase)
    is Groove -> DrumParser.readNotation(notation.notationId, notation.name, text, music.time)
}

/** Writes [notation] as the text [readNotation] reads. */
fun writeNotation(notation: Notation, music: Music): String = when (notation) {
    is Progression -> ChordParser(music.key).writeNotation(notation, music.time)
    is Phrase -> MelodyParser(music.key).writeNotation(notation, music.time)
    is Stanza -> LyricParser.writeNotation(notation, music.time)
    is Groove -> DrumParser.writeNotation(notation, music.time)
}
