import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventId
import streetlight.model.data.NewRequest
import streetlight.model.data.SongId
import streetlight.web.io.ApiClient

class EventPortal(
    private val scope: CoroutineScope,
    private val eventId: EventId,
    private val api: ApiClient,
) {
    private val view = storeOf(EventPortalState())

    fun setSongId(songId: SongId, ) {
        view.setValue { it.copy(songId = songId) }
    }

    fun sendRequest() {
        val songId = view.now.songId ?: return
        val request = NewRequest(
            eventId = eventId,
            songId = songId,
            songName = null,
            isJoining = false,
            comment = null,
            requesterName = null
        )
    }
}

data class EventPortalState(
    val songId: SongId? = null,
)