package streetlight.app.utils

import pondui.ui.services.MidiChord
import pondui.ui.services.MidiSequence
import streetlight.model.data.ChordHelper
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.data.notationOf

fun SongPart.toMidiSequence(
    beatsPerMeasure: Int,
    rootPitch: Int = 60,
    capo: Int?,
    tempo: Int?,
): MidiSequence {
    val chords = mutableListOf<MidiChord>()
    composition.forEachIndexed { index, sectionIndex ->
        val section = sections.getOrNull(sectionIndex) ?: return@forEachIndexed
        repeat(section.repetitions) {
            section.chords.forEach { chord ->
                val midiChord = chord.toMidiChord(beatsPerMeasure, rootPitch, capo)
                chords.add(midiChord)
            }
        }
    }
    return MidiSequence(tempo = tempo, chords = chords)
}

fun SongSection.toMidiSequence(
    beatsPerMeasure: Int,
    rootPitch: Int = 60,
    capo: Int? = null,
    tempo: Int? = null,
): MidiSequence {
    val chords = mutableListOf<MidiChord>()
    this.chords.forEach { chord ->
        val midiChord = chord.toMidiChord(beatsPerMeasure, rootPitch, capo)
        chords.add(midiChord)
    }
    return MidiSequence(tempo = tempo, chords = chords)
}

fun MeasureChord.toMidiChord(beatsPerMeasure: Int, rootPitch: Int, capo: Int?): MidiChord {
    val beats = duration ?: beatsPerMeasure
    return expression?.toNotation(rootPitch)?.let {
        ChordHelper.map[it]?.let { notes -> capo?.let { capo -> notes.map { it + capo } } ?: notes }
    }?.let {
        MidiChord(
            beats = beats,
            notes = it
        )
    } ?: MidiChord(beats, null)
}