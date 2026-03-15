package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient

class UserCache(
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    val talent = ItemCache(scope, { it.talentId }) { api.readTalents() }
    val song = ItemCache(scope, { it.songId }) { api.readSongs() }
    val file = ItemCache(scope, { it }) { api.readUserFiles() }
    val galaxy = ItemCache(scope, { it.galaxyId }) { api.readGalaxies() }

    fun reset() {
        talent.clear()
        song.clear()
        file.clear()
        galaxy.clear()
    }
}