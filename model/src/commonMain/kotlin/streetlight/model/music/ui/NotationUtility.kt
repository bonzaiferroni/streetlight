package streetlight.model.music.ui

import streetlight.model.music.Key

fun NoteEvent.toChord(key: Key) = (chord?.let { midiChordOf(it, key) } ?: listOf(staffStepOf(midi, key)))
fun NoteEvent.toMeasureOnset(measureTick: Int, measureDuration: Int) = (tick - measureTick).toDouble() / measureDuration