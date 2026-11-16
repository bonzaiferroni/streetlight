package streetlight.web.model

import streetlight.model.music.NotationId
import streetlight.model.music.Track

/** What the music editor shows in place of its overview, seeded with the track as it stood when focused. */
sealed interface MusicEditorFocus {
    val track: Track
}

data class TrackFocus(
    override val track: Track,
) : MusicEditorFocus

data class NotationFocus(
    override val track: Track,
    val notationId: NotationId,
) : MusicEditorFocus
