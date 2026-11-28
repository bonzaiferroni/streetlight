package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.lyricsOf
import streetlight.model.music.trackOf
import streetlight.model.music.trackOrNullOf

fun MusicSheet.musicSourceOf(
    targetNotationId: NotationId?,
    targetTrackId: TrackId?,
): MusicSource {
    val measureTicks = music.time.measure.ticks
    // tick 0 is the first downbeat of the target: the notation, or the song
    val targetTicks = targetNotationId?.let { trackOf(it).spansOf(it, measureTicks)?.firstOrNull()?.ticks }
        ?: music.arrangement.sumOf { music.lengthOf(it)?.ticks ?: 0 }
    val measureCount = (targetTicks + measureTicks - 1) / measureTicks
    val trackSources = tracks.mapNotNull { track ->
        if (targetTrackId != null && track.trackId != targetTrackId) return@mapNotNull null
        val lyrics = tracks.lyricsOf(track)
        val spans: List<NotationSpan>
        val lyricSpans: List<NotationSpan>
        if (targetNotationId == null) {
            spans = songSpansOf(track)
            lyricSpans = lyrics?.let { songSpansOf(it) }.orEmpty()
        } else {
            spans = track.spansOf(targetNotationId, measureTicks) ?: return@mapNotNull null
            // the lyrics of each section the notation plays, each from its first downbeat
            val sectionIds = music.arrangement.filter { track.sections[it] == targetNotationId }.distinct()
            lyricSpans = lyrics?.let { lyricTrack ->
                sectionIds.flatMap { sectionId -> lyricTrack.sections[sectionId]?.let { lyricTrack.spansOf(it, measureTicks) }.orEmpty() }
            }.orEmpty()
        }
        val lyricEvents = lyrics?.let { lyricTrack -> lyricSpans.flatMap { lyricTrack.eventsOf(it, music.key) } }.orEmpty()
        val events = (spans.flatMap { track.eventsOf(it, music.key) } + lyricEvents).sortedBy { it.tick }

        // measure 0 is the pickup, ending at the first downbeat, and each after it ends a measure later
        var eventIndex = 0
        val measures = List(measureCount + 1) { measureIndex ->
            val eventStart = eventIndex
            while (eventIndex < events.size && events[eventIndex].tick < measureIndex * measureTicks) eventIndex++
            MeasureSpan(eventStart, eventIndex)
        }
        val layout = NotationLayout(spans.flatMap { track.linesOf(it, measureTicks) }, staffWindowOf(track.trackId))
        TrackSource(track.trackId, track.name, events, measures, spans, layout, track.trackType, track.capo)
    }
    val pickupTicks = trackSources.maxOfOrNull { -(it.events.firstOrNull()?.tick ?: 0) }?.coerceAtLeast(0) ?: 0

    return MusicSource(
        name = music.name,
        targetId = targetNotationId,
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

/** Each notation and arrangement [track] plays through the song, placed from its first downbeat. */
private fun MusicSheet.songSpansOf(track: Track): List<NotationSpan> {
    var sectionStart = 0
    return music.arrangement.flatMap { sectionId ->
        val start = sectionStart
        sectionStart += music.lengthOf(sectionId)?.ticks ?: 0
        val notationId = track.sections[sectionId] ?: return@flatMap emptyList()
        track.spansOf(notationId, music.time.measure.ticks).orEmpty().map { it.copy(start = start + it.start) }
    }
}

/** The notation [span] plays in this track, or `null` for an arrangement. */
private fun Track.notationOf(span: NotationSpan) =
    notations.firstOrNull { it.notationId == span.notationId }.takeIf { !span.isArrangement }

/** The events [span] plays in this track, its pitches in [key], none for an arrangement. */
private fun Track.eventsOf(span: NotationSpan, key: Key): List<NotationEvent> {
    val notation = notationOf(span) ?: return emptyList()
    val notationId = notation.notationId
    val events = mutableListOf<NotationEvent>()
    var tick = span.start - span.pickup.ticks
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
            tick = span.start
            lane.beats.forEachIndexed { index, beat ->
                addEvent(DrumEvent(lane.drum, beat.hit, tick, beat.beats, notationId, index))
            }
        }
    }
    return events
}

/** The lines [span] fills in this track, as spans of measures [measureTicks] long, none for an arrangement. */
private fun Track.linesOf(span: NotationSpan, measureTicks: Int): List<MeasureLine> {
    val perLine = notationOf(span)?.measuresPerLine?.coerceAtLeast(1) ?: return emptyList()
    val line = span.measureLineOf(measureTicks)
    // the pickup measure leads the first line, so lines step from the first measure after it
    return (maxOf(line.measureStart, 1) until line.measureEnd step perLine).map { lineStart ->
        MeasureLine(if (lineStart == 1) line.measureStart else lineStart, minOf(lineStart + perLine, line.measureEnd))
    }
}

/**
 * Each time [notationId] or a notation or arrangement it holds starts in this track, from 0, with its wait in measures
 * [measureTicks] long, or `null` when the track does not define it or an arrangement holds itself. [notationId] comes
 * first, spanning the whole.
 */
fun Track.spansOf(notationId: NotationId, measureTicks: Int): List<NotationSpan>? =
    spansOf(notationId, measureTicks, emptySet())

private fun Track.spansOf(notationId: NotationId, measureTicks: Int, visited: Set<NotationId>): List<NotationSpan>? {
    if (notationId in visited) return null
    notations.firstOrNull { it.notationId == notationId }?.let { notation ->
        return listOf(NotationSpan(notationId, notation.label, 0, notation.length.ticks, notation.downbeat, false))
    }
    val arrangement = arrangements.firstOrNull { it.notationId == notationId } ?: return null
    val held = arrangement.notations.map { spansOf(it, measureTicks, visited + notationId) ?: return null }
    val wait = (arrangement.wait ?: 0) * measureTicks
    var start = wait
    val played = List(arrangement.repeats ?: 1) { held }.flatten().flatMap { heldSpans ->
        heldSpans.map { it.copy(start = start + it.start) }.also { start += heldSpans.first().ticks }
    }
    // the first pickup reaches back through the wait, and before the arrangement only past it
    val pickup = Beats(((played.firstOrNull()?.pickup?.ticks ?: 0) - wait).coerceAtLeast(0))
    return listOf(NotationSpan(notationId, arrangement.name, 0, start, pickup, true)) + played
}
