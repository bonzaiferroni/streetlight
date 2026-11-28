package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.trackOrNullOf

fun MusicSheet.musicSourceOf(
    targetNotationId: NotationId?,
    targetTrackId: TrackId?,
): MusicSource {
    val measureTicks = music.time.measure.ticks
    val songTicks = music.arrangement.sumOf { music.lengthOf(it)?.ticks ?: 0 }
    val measureCount = (songTicks + measureTicks - 1) / measureTicks
    val trackSources = tracks.mapNotNull { track ->
        if (targetTrackId != null && track.trackId != targetTrackId) return@mapNotNull null
        val events = mutableListOf<NotationEvent>()
        val lines = mutableListOf<NotationLayoutLine>()
        var sectionStart = 0
        music.arrangement.forEach { sectionId ->
            var notationStart = sectionStart
            sectionStart += music.lengthOf(sectionId)?.ticks ?: 0
            val notationId = track.sections[sectionId] ?: return@forEach
            if (targetNotationId != null && notationId != targetNotationId) return@forEach

            val passes = track.arrangementOf(notationId) ?: return@forEach
            passes.forEach { pass ->
                val notation = track.notations.first { it.notationId == pass.notationId }
                var tick = notationStart - notation.downbeat.ticks
                fun addEvent(event: NotationEvent) {
                    events.add(event)
                    tick += event.beats.ticks
                }
                when (notation) {
                    is Phrase -> notation.notes.forEachIndexed { noteIndex, note ->
                        addEvent(NoteEvent(note.interval?.toMidi(music.key), tick, note.beats, notation.notationId, noteIndex))
                    }
                    is Progression -> notation.notes.forEachIndexed { noteIndex, note ->
                        addEvent(ChordEvent(note.chord, tick, note.beats, notation.notationId, noteIndex))
                    }
                    is Stanza -> notation.notes.forEachIndexed { noteIndex, note ->
                        addEvent(VocalEvent(note.text, tick, note.beats, notation.notationId, noteIndex))
                    }
                    is Groove -> notation.lanes.forEach { lane ->
                        tick = notationStart
                        lane.beats.forEachIndexed { beatIndex, beat ->
                            addEvent(DrumEvent(beat.hit?.let { lane.drum }, tick, beat.beats, notation.notationId, beatIndex))
                        }
                    }
                }
                // measure 0 is the pickup, so the notation's first measure follows the measures before it
                val firstMeasure = notationStart / measureTicks + 1
                val endMeasure = firstMeasure + (notation.length.ticks + measureTicks - 1) / measureTicks
                val perLine = notation.measuresPerLine.coerceAtLeast(1)
                for (lineStart in firstMeasure until endMeasure step perLine) {
                    // the first line leads with the pickup
                    lines.add(NotationLayoutLine(if (lineStart == 1) 0 else lineStart, minOf(lineStart + perLine, endMeasure)))
                }
                notationStart += notation.length.ticks
            }
        }
        events.sortBy { it.tick }

        // measure 0 is the pickup, ending at the first downbeat, and each after it ends a measure later
        var eventIndex = 0
        val measures = List(measureCount + 1) { measureIndex ->
            val eventStart = eventIndex
            while (eventIndex < events.size && events[eventIndex].tick < measureIndex * measureTicks) eventIndex++
            MeasureSpan(eventStart, eventIndex)
        }
        TrackSource(track.name, events, measures, ProtoNotationLayout(lines, staffWindowOf(track.trackId)), track.trackType)
    }
    val pickupTicks = trackSources.maxOfOrNull { -(it.events.firstOrNull()?.tick ?: 0) }?.coerceAtLeast(0) ?: 0

    return MusicSource(
        name = music.name,
        pickup = Beats(pickupTicks),
        measureCount = measureCount,
        tracks = trackSources,
        time = music.time,
        key = music.key,
        bpm = music.bpm,
        capo = music.capo,
    )
}

fun MusicSheet.musicSourceOf(notationId: NotationId): MusicSource? {
    val track = trackOrNullOf(notationId) ?: return null
    return musicSourceOf(notationId, track.trackId)
}

fun Track.arrangementOf(notationId: NotationId): List<NotationPass>? = arrangementOf(notationId, emptySet())

private fun Track.arrangementOf(notationId: NotationId, visited: Set<NotationId>): List<NotationPass>? {
    if (notationId in visited) return null
    notations.firstOrNull { it.notationId == notationId }?.let { return listOf(NotationPass(notationId)) }
    val arrangement = arrangements.firstOrNull { it.notationId == notationId } ?: return null
    val list = arrangement.notations.flatMap { arrangementOf(it, visited + notationId) ?: return null }
    return arrangement.repeats?.let { (1..it).flatMap { list } } ?: list
}

data class NotationPass(
    val notationId: NotationId,
    val start: Int = 0,
)