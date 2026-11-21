package streetlight.web.model

import streetlight.model.music.NotationId
import streetlight.model.music.Track
import streetlight.model.music.TrackId

/** What the music editor shows in place of its overview, seeded with the track as it stood when focused. */
sealed interface MusicEditorFocus {
    val trackId: TrackId
}

data class TrackFocus(
    override val trackId: TrackId,
) : MusicEditorFocus

data class NotationFocus(
    override val trackId: TrackId,
    val notationId: NotationId,
) : MusicEditorFocus
