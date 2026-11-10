package streetlight.model.music.text

import streetlight.model.music.Groove
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Section
import streetlight.model.music.Stanza

/**
 * Reads [text], the notation of [section] alone, in the notation of its track type against [music], chords spelled in
 * [style]. The section read keeps what its notation does not state, and a stanza is checked against the phrase
 * [melody] holds for its section.
 */
fun readSection(
    section: Section,
    text: String,
    music: Music,
    style: NotationStyle,
    melody: MelodyTrack? = null,
): NotationRead<Section> = when (section) {
    is Progression -> ChordParser(music.key, style).readSection(section.sectionId, text, music.time)
    is Phrase -> MelodyParser(music.key).readSection(section.sectionId, text, music.time).let { read ->
        NotationRead(read.value.copy(lyrics = section.lyrics), read.errors)
    }
    is Stanza -> {
        val phrase = melody?.sections?.firstOrNull { it.sectionId == section.sectionId }
        LyricParser.readSection(section.sectionId, text, music.time, phrase)
    }
    is Groove -> DrumParser.readSection(section.sectionId, text, music.time)
}

/** Writes [section] as the text [readSection] reads. */
fun writeSection(section: Section, music: Music, style: NotationStyle): String = when (section) {
    is Progression -> ChordParser(music.key, style).writeSection(section, music.time)
    is Phrase -> MelodyParser(music.key).writeSection(section, music.time)
    is Stanza -> LyricParser.writeSection(section, music.time)
    is Groove -> DrumParser.writeSection(section, music.time)
}
