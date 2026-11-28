package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
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
        val passes = track.passesOf(spans)
        val lyricPasses = lyrics?.passesOf(lyricSpans).orEmpty()
        val events = (passes + lyricPasses).flatMap { eventsOf(it, music.key) }.sortedBy { it.tick }

        // measure 0 is the pickup, ending at the first downbeat, and each after it ends a measure later
        var eventIndex = 0
        val measures = List(measureCount + 1) { measureIndex ->
            val eventStart = eventIndex
            while (eventIndex < events.size && events[eventIndex].tick < measureIndex * measureTicks) eventIndex++
            MeasureSpan(eventStart, eventIndex)
        }
        val layout = NotationLayout(passes.flatMap { linesOf(it, measureTicks) }, staffWindowOf(track.trackId))
        TrackSource(track.trackId, track.name, events, measures, passes, spans, layout, track.trackType, track.capo)
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

/** The plays of the notations among [spans], leaving out the arrangements that hold them. */
private fun Track.passesOf(spans: List<NotationSpan>) = spans.mapNotNull { span ->
    notations.firstOrNull { it.notationId == span.notationId }?.let { NotationPass(it, span.start) }
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
                addEvent(DrumEvent(lane.drum, beat.hit, tick, beat.beats, notationId, index))
            }
        }
    }
    return events
}

/** The lines of [pass], as spans of measures [measureTicks] long, the first line of the song leading with the pickup. */
private fun linesOf(pass: NotationPass, measureTicks: Int): List<MeasureLine> {
    val notation = pass.notation
    // measure 0 is the pickup, so the notation's first measure follows the measures before it
    val firstMeasure = pass.start / measureTicks + 1
    val endMeasure = firstMeasure + (notation.length.ticks + measureTicks - 1) / measureTicks
    val perLine = notation.measuresPerLine.coerceAtLeast(1)
    return (firstMeasure until endMeasure step perLine).map { lineStart ->
        MeasureLine(if (lineStart == 1) 0 else lineStart, minOf(lineStart + perLine, endMeasure))
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
    notations.firstOrNull { it.notationId == notationId }?.let { return listOf(NotationSpan(notationId, 0, it.length.ticks)) }
    val arrangement = arrangements.firstOrNull { it.notationId == notationId } ?: return null
    val held = arrangement.notations.map { spansOf(it, measureTicks, visited + notationId) ?: return null }
    var start = (arrangement.wait ?: 0) * measureTicks
    val played = List(arrangement.repeats ?: 1) { held }.flatten().flatMap { heldSpans ->
        heldSpans.map { it.copy(start = start + it.start) }.also { start += heldSpans.first().ticks }
    }
    return listOf(NotationSpan(notationId, 0, start)) + played
}

/** A time a track plays [notation], its first downbeat at [start]. */
data class NotationPass(
    val notation: Notation,
    val start: Int,
)
