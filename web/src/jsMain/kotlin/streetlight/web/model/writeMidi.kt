package streetlight.web.model

import streetlight.model.music.Beats
import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import streetlight.model.music.DrumTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.web.utility.downloadFile

/** Hands the viewer the tracks of [song] as a MIDI file, laid along its arrangement. */
fun exportMidi(song: StoredSong) {
    val trackTones = songTonesOf(song.music, song.tracks, song.tracks).filter { it.isSounding }
    downloadFile(writeMidi(song.music, trackTones), "${song.song.title}.mid", MIDI_TYPE)
}

/**
 * Writes [trackTones] as a Standard MIDI File of format 1: a conductor track holding the tempo and time signature of
 * [music], then a track of notes for each, each on its own channel. Drum tracks share channel 10, as General MIDI
 * keeps it.
 */
fun writeMidi(music: Music, trackTones: List<TrackTones>): ByteArray {
    // a pickup starts before tick 0, and a MIDI file cannot, so every event shifts by the earliest
    val firstTick = trackTones.mapNotNull { it.firstTick }.minOrNull() ?: 0
    val shift = -minOf(firstTick, 0)
    var pitchedIndex = 0
    val chunks = listOf(conductorChunk(music)) + trackTones.map { tones ->
        val channel = if (tones.track is DrumTrack) DRUM_CHANNEL else channelOf(pitchedIndex++)
        noteChunk(tones, channel, shift)
    }
    val file = buildBytes {
        chunk("MThd") {
            short(1)
            short(chunks.size)
            short(Beats.TICKS_PER_BEAT)
        }
        chunks.forEach { addAll(it) }
    }
    return file.toByteArray()
}

/** The track that sets the tempo and time signature, which every other track follows. */
private fun conductorChunk(music: Music) = buildBytes {
    chunk("MTrk") {
        // the tempo counts beats of the time signature, and MIDI counts microseconds per quarter note
        val microsPerQuarter = 60_000_000L * Beats.TICKS_PER_BEAT / music.bpm / music.time.beat.ticks
        event(0, 0xFF, 0x51, 3)
        add((microsPerQuarter shr 16).toByte())
        add((microsPerQuarter shr 8).toByte())
        add(microsPerQuarter.toByte())
        event(0, 0xFF, 0x58, 4, music.time.beats, log2(music.time.beatUnit), 24, 8)
        event(0, 0xFF, 0x2F, 0)
    }
}

/** The notes of [tones] on [channel], each event moved [shift] ticks later. */
private fun noteChunk(tones: TrackTones, channel: Int, shift: Int) = buildBytes {
    chunk("MTrk") {
        val name = tones.track.trackId.name.encodeToByteArray()
        event(0, 0xFF, 0x03, name.size)
        addAll(name.toList())
        val velocity = if (tones.track is MelodyTrack) MELODY_VELOCITY else ACCOMPANIMENT_VELOCITY
        val pitched = tones.tones.flatMap { tone ->
            tone.midi.flatMap { midi ->
                listOf(
                    NoteEvent(tone.startTick + shift, 0x90 or channel, midi, velocity),
                    NoteEvent(tone.startTick + tone.ticks + shift, 0x80 or channel, midi, 0),
                )
            }
        }
        // a drum has no length, so each hit sounds for a short fixed time
        val drums = tones.drums.flatMap { drum ->
            val note = drum.drum.toMidi()
            listOf(
                NoteEvent(drum.startTick + shift, 0x90 or channel, note, drum.hit.toVelocity()),
                NoteEvent(drum.startTick + DRUM_NOTE_TICKS + shift, 0x80 or channel, note, 0),
            )
        }
        val events = (pitched + drums)
            // a note that ends where the next of its pitch starts must end first, or the start is cut short
            .sortedWith(compareBy({ it.tick }, { if (it.status and 0xF0 == 0x80) 0 else 1 }))
        var tick = 0
        events.forEach { event ->
            event(event.tick - tick, event.status, event.midi.coerceIn(0, 127), event.velocity)
            tick = event.tick
        }
        event(0, 0xFF, 0x2F, 0)
    }
}

private class NoteEvent(val tick: Int, val status: Int, val midi: Int, val velocity: Int)

/** The General MIDI note of the drum on channel 10. */
private fun Drum.toMidi() = when (this) {
    Drum.Kick -> 36
    Drum.Snare -> 38
    Drum.HiHat -> 42
    Drum.Crash -> 49
    Drum.Ride -> 51
    Drum.Tom -> 45
}

private fun DrumHit.toVelocity() = when (this) {
    DrumHit.Normal -> 90
    DrumHit.Accent -> 120
    DrumHit.Ghost -> 40
}

/** The channel of the pitched track at [index], passing over channel 10, which General MIDI keeps for drums. */
private fun channelOf(index: Int) = (if (index >= DRUM_CHANNEL) index + 1 else index) % 16

private fun log2(value: Int) = 31 - value.countLeadingZeroBits()

private fun buildBytes(block: MutableList<Byte>.() -> Unit) = mutableListOf<Byte>().apply(block)

/** Adds a chunk of [type] holding what [block] adds, led by its length. */
private fun MutableList<Byte>.chunk(type: String, block: MutableList<Byte>.() -> Unit) {
    val body = buildBytes(block)
    addAll(type.encodeToByteArray().toList())
    int(body.size)
    addAll(body)
}

/** Adds an event [delta] ticks after the one before, as its delta time and then [bytes]. */
private fun MutableList<Byte>.event(delta: Int, vararg bytes: Int) {
    variableLength(delta)
    bytes.forEach { add(it.toByte()) }
}

private fun MutableList<Byte>.short(value: Int) {
    add((value shr 8).toByte())
    add(value.toByte())
}

private fun MutableList<Byte>.int(value: Int) {
    short(value shr 16)
    short(value)
}

/** Adds [value] as a MIDI variable-length quantity: seven bits a byte, each but the last flagged by its high bit. */
private fun MutableList<Byte>.variableLength(value: Int) {
    var remaining = value shr 7
    val groups = mutableListOf(value and 0x7F)
    while (remaining > 0) {
        groups.add(0, (remaining and 0x7F) or 0x80)
        remaining = remaining shr 7
    }
    groups.forEach { add(it.toByte()) }
}

private const val DRUM_CHANNEL = 9
private const val DRUM_NOTE_TICKS = 120
private const val MELODY_VELOCITY = 100
private const val ACCOMPANIMENT_VELOCITY = 80

private const val MIDI_TYPE = "audio/midi"
