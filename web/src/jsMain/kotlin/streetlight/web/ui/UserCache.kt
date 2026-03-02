package streetlight.web.ui

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope

class UserCache(
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    val talents = ItemCache(scope, { it.talentId }) { api.readTalents() }
    val songs = ItemCache(scope, { it.songId }) { api.readSongs() }
    val files = ItemCache(scope, { it }) { api.readUserFiles().also{console.log("yer files")} }

    fun reset() {
        talents.clear()
        songs.clear()
    }
}