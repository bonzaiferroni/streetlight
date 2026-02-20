package streetlight.web

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope

class UserCache(
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    val talents = ItemCache(scope) { api.readTalents() }
    val songs = ItemCache(scope) { api.readSongs() }

    fun reset() {
        talents.clear()
        songs.clear()
    }
}