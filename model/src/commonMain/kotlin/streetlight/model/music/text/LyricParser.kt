package streetlight.model.music.text

import streetlight.model.music.LyricNote
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Phrase
import streetlight.model.music.NotationId
import streetlight.model.music.Stanza
import streetlight.model.music.TimeSignature
import streetlight.model.music.MusicId
import streetlight.model.music.TrackId

/**
 * The parser of a lyric track's text, which keeps its own time with the lengths of the melody's jianpu.
 *
 * A syllable or word is a quarter note long in every meter, and keeps the `-` that joins it to the next. `_` holds the
 * syllable before for a quarter note, and `0` is a rest. Then `\` halves any of them, `\\` quarters it, and `.` adds
 * half its length, as `ma-\.`. The tokens before the first bar line of a stanza are its pickup. A token that cannot be
 * read is reported and skipped.
 */
object LyricParser {
    /**
     * Reads the lyric track [trackId] of the music [musicId], named [name], from [text], with measures checked against
     * [time] and the music's sections found in [names].
     *
     * Each stanza is checked against the phrase [melody] plays for the first section the stanza plays: every syllable
     * starts on a note of the melody, and the stanza ends with it or before.
     */
    fun read(
        trackId: TrackId,
        musicId: MusicId,
        name: String,
        text: String,
        time: TimeSignature,
        names: SectionNames,
        melody: MelodyTrack? = null,
    ): NotationParse<LyricTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors, names)
        val stanzas = trackText.notations.map { notation ->
            val sectionId = trackText.sections.entries.firstOrNull { it.value == notation.notationId }?.key
            val phraseId = sectionId?.let { melody?.sections?.get(it) }
            val phrase = melody?.notations?.firstOrNull { it.notationId == phraseId }
            readNotation(notation, time, phrase, errors)
        }
        val track = LyricTrack(trackId, musicId, name, stanzas, trackText.arrangements, trackText.sections, trackText.capo)
        return NotationParse(track, errors)
    }

    /**
     * Reads the notation [notationId] named [name] from [text], the lines of its measures alone, checked against [time]
     * and against [phrase], the melody sung with it, when there is one.
     */
    fun readNotation(
        notationId: NotationId,
        name: String?,
        text: String,
        time: TimeSignature,
        phrase: Phrase? = null,
    ): NotationParse<Stanza> {
        val errors = mutableListOf<NotationError>()
        return NotationParse(readNotation(notationTextOf(notationId, name, text, errors), time, phrase, errors), errors)
    }

    private fun readNotation(
        notation: NotationText,
        time: TimeSignature,
        phrase: Phrase?,
        errors: MutableList<NotationError>,
    ): Stanza {
        val notes = mutableListOf<LyricNote>()
        val pickup = mutableListOf<LyricNote>()
        val noteLines = mutableListOf<Int>()
        val segments = notation.toSegments()
        segments.pickup?.let { segment ->
            fun error(message: String) = errors.add(NotationError(segment.line, message))
            val ticks = readSegment(segment.text, pickup, ::error)
            pickupProblemOf(ticks, time)?.let(::error)
            repeat(pickup.size) { noteLines.add(segment.line) }
        }
        segments.measures.forEachIndexed { index, segment ->
            fun error(message: String) = errors.add(NotationError(segment.line, message))
            val ticks = readSegment(segment.text, notes, ::error)
            measureProblemOf(ticks, index == segments.measures.lastIndex, time)?.let(::error)
            repeat(pickup.size + notes.size - noteLines.size) { noteLines.add(segment.line) }
        }
        val stanza = Stanza(notation.notationId, notation.name, notes, pickup, measuresPerLineOf(notation.lines, MEASURES_PER_LINE))
        if (phrase != null && noteLines.isNotEmpty()) errors.addAll(alignmentErrorsOf(stanza, noteLines, phrase))
        return stanza
    }

    /** Writes [track] as the text [read] reads, with measures cut by [time] and sections named by [names]. */
    fun write(track: LyricTrack, time: TimeSignature, names: SectionNames) =
        writeTrackText(track, track.notations.map { linesOf(it, time) }, names)

    /** Writes [stanza] as the text [readNotation] reads, with measures cut by [time]. */
    fun writeNotation(stanza: Stanza, time: TimeSignature) = linesOf(stanza, time).toLines().joinToString("\n")

    private fun linesOf(stanza: Stanza, time: TimeSignature) = NotationLines(
        notationId = stanza.notationId,
        name = stanza.name,
        pickup = stanza.pickup.takeIf { it.isNotEmpty() }?.let { pickup ->
            pickup.flatMap { writeNote(it, it.beats.ticks, false) }.joinToString(" ")
        },
        measures = writeMeasures(stanza.notes, time.measure, { it.beats.ticks }, ::writeNote),
        measuresPerLine = stanza.measuresPerLine,
    )

    /** Reads the tokens of [text] onto the end of [notes], returning the ticks they filled. */
    private fun readSegment(text: String, notes: MutableList<LyricNote>, error: (String) -> Unit): Int {
        var ticks = 0
        for (token in tokensOf(text)) {
            val match = TOKEN.matchEntire(token)
            if (match == null) {
                error("Unknown lyric $token")
                continue
            }
            val (word, halving, dot) = match.destructured
            val beats = lengthOf(halving, dot, QUARTER)
            when {
                word == HOLD -> {
                    val last = notes.lastOrNull()
                    if (last?.text == null) {
                        error("A hold with no syllable before it")
                        continue
                    }
                    notes[notes.lastIndex] = last.copy(beats = last.beats + beats)
                }
                word == REST -> notes.add(LyricNote(null, beats))
                word.none { it.isLetterOrDigit() } -> {
                    error("Unknown lyric $token")
                    continue
                }
                else -> notes.add(LyricNote(word, beats))
            }
            ticks += beats.ticks
        }
        return ticks
    }

    /**
     * The problems of [stanza] against [phrase], their pickups meeting at the first bar line. [noteLines] holds the line
     * of each note of the stanza, its pickup first.
     */
    private fun alignmentErrorsOf(stanza: Stanza, noteLines: List<Int>, phrase: Phrase): List<NotationError> {
        val melodyNotes = phrase.pickup + phrase.notes
        val melodyStart = -phrase.pickup.sumOf { it.beats.ticks }
        val melodyOnsets = melodyNotes.runningFold(melodyStart) { onset, note -> onset + note.beats.ticks }
        val sungOnsets = melodyNotes.indices.filter { melodyNotes[it].interval != null }.map { melodyOnsets[it] }.toSet()
        val melodyEnd = melodyOnsets.last()

        val lyricNotes = stanza.pickup + stanza.notes
        val lyricStart = -stanza.pickup.sumOf { it.beats.ticks }
        val lyricOnsets = lyricNotes.runningFold(lyricStart) { onset, note -> onset + note.beats.ticks }

        val errors = lyricNotes.indices.filter { index ->
            val onset = lyricOnsets[index]
            lyricNotes[index].text != null && onset < melodyEnd && onset !in sungOnsets
        }.map { NotationError(noteLines[it], "${lyricNotes[it].text} starts off the melody's notes") }
        return if (lyricOnsets.last() > melodyEnd) {
            errors + NotationError(noteLines.last(), "The lyrics run past the melody")
        } else errors
    }

    /** Writes [ticks] of [note] as tokens. A piece that carries on a note already written holds it. */
    private fun writeNote(note: LyricNote, ticks: Int, isContinuation: Boolean) =
        lengthSuffixesOf(ticks).mapIndexed { index, suffix ->
            val word = when {
                note.text == null -> REST
                index == 0 && !isContinuation -> note.text
                else -> HOLD
            }
            "$word$suffix"
        }

    /** The length marks of the tokens that fill [ticks]: a dot alone where it can, else each quarter and what is left. */
    private fun lengthSuffixesOf(ticks: Int): List<String> {
        val quarter = QUARTER.ticks
        if (ticks == quarter * 3 / 2) return listOf(DOT)
        return List(ticks / quarter) { "" } + fractionSuffixesOf(ticks % quarter, quarter)
    }
}

/** The measures on each line of a stanza when its text gives none. */
private const val MEASURES_PER_LINE = 2

/** A word with its length marks, a word holding no backslash. */
private val TOKEN = Regex("""^([^\\]*?)(\\{0,2})(\.?)$""")
private const val HOLD = "_"
private const val REST = "0"
