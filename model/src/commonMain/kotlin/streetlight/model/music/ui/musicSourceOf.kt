package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.SectionId
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lyricsOf
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
        val sectionIds = music.arrangement.filter { targetNotationId == null || track.sections[it] == targetNotationId }.toSet()
        val passes = passesOf(track, sectionIds)
        val lyricPasses = tracks.lyricsOf(track)?.let { passesOf(it, sectionIds) }.orEmpty()
        val events = (passes + lyricPasses).flatMap { eventsOf(it, music.key) }.sortedBy { it.tick }

        // measure 0 is the pickup, ending at the first downbeat, and each after it ends a measure later
        var eventIndex = 0
        val measures = List(measureCount + 1) { measureIndex ->
            val eventStart = eventIndex
            while (eventIndex < events.size && events[eventIndex].tick < measureIndex * measureTicks) eventIndex++
            MeasureSpan(eventStart, eventIndex)
        }
        val layout = ProtoNotationLayout(passes.flatMap { linesOf(it, measureTicks) }, staffWindowOf(track.trackId))
        TrackSource(track.trackId, track.name, events, measures, passes, layout, track.trackType, track.capo)
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

/** Each notation [track] plays in [sectionIds], placed from the start of the song. */
private fun MusicSheet.passesOf(track: Track, sectionIds: Set<SectionId>): List<NotationPass> {
    var sectionStart = 0
    return music.arrangement.flatMap { sectionId ->
        val start = sectionStart
        sectionStart += music.lengthOf(sectionId)?.ticks ?: 0
        val notationId = track.sections[sectionId]?.takeIf { sectionId in sectionIds } ?: return@flatMap emptyList()
        track.arrangementOf(notationId).orEmpty().map { it.copy(start = start + it.start) }
    }
}

/** The events of [pass], its pitches in [key]. */
private fun eventsOf(pass: NotationPass, key: Key): List<NotationEvent> {
    val notation = pass.notation
    val notationId = notation.notationId
    val events = mutableListOf<NotationEvent>()
    var tick = pass.start - notation.downbeat.ticks
    fun addEvent(event: NotationEvent) {
        events.add(event)
        tick += event.beats.ticks
    }
    when (notation) {
        is Phrase -> notation.notes.forEachIndexed { index, note ->
            addEvent(NoteEvent(note.interval?.toMidi(key), tick, note.beats, notationId, index))
        }
        is Progression -> notation.notes.forEachIndexed { index, note ->
            addEvent(ChordEvent(note.chord, tick, note.beats, notationId, index))
        }
        is Stanza -> notation.notes.forEachIndexed { index, note ->
            addEvent(VocalEvent(note.text, tick, note.beats, notationId, index))
        }
        is Groove -> notation.lanes.forEach { lane ->
            tick = pass.start
            lane.beats.forEachIndexed { index, beat ->
                addEvent(DrumEvent(beat.hit?.let { lane.drum }, tick, beat.beats, notationId, index))
            }
        }
    }
    return events
}

/** The lines of [pass], as spans of measures [measureTicks] long, the first line of the song leading with the pickup. */
private fun linesOf(pass: NotationPass, measureTicks: Int): List<NotationLayoutLine> {
    val notation = pass.notation
    // measure 0 is the pickup, so the notation's first measure follows the measures before it
    val firstMeasure = pass.start / measureTicks + 1
    val endMeasure = firstMeasure + (notation.length.ticks + measureTicks - 1) / measureTicks
    val perLine = notation.measuresPerLine.coerceAtLeast(1)
    return (firstMeasure until endMeasure step perLine).map { lineStart ->
        NotationLayoutLine(if (lineStart == 1) 0 else lineStart, minOf(lineStart + perLine, endMeasure))
    }
}

fun Track.arrangementOf(notationId: NotationId): List<NotationPass>? = arrangementOf(notationId, emptySet())

private fun Track.arrangementOf(notationId: NotationId, visited: Set<NotationId>): List<NotationPass>? {
    if (notationId in visited) return null
    notations.firstOrNull { it.notationId == notationId }?.let { return listOf(NotationPass(it, 0)) }
    val arrangement = arrangements.firstOrNull { it.notationId == notationId } ?: return null
    val held = arrangement.notations.flatMap { heldId ->
        arrangementOf(heldId, visited + notationId)?.map { it.notation } ?: return null
    }
    var start = 0
    return List(arrangement.repeats ?: 1) { held }.flatten().map { notation ->
        NotationPass(notation, start).also { start += notation.length.ticks }
    }
}

/** A time a track plays [notation], its first downbeat at [start]. */
data class NotationPass(
    val notation: Notation,
    val start: Int,
)
