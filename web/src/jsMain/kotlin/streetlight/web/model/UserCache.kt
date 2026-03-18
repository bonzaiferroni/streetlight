package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient

class UserCache(
    scope: CoroutineScope,
    private val api: ApiClient,
    private val gate: UserGate,
) {
    init {
        scope.launch {
            gate.userFlow.collect { user ->
                if (user == null) {
                    reset()
                }
            }
        }
    }

    val talent = ItemCache(scope, { it.talentId }) { api.readTalents() }
    val song = ItemCache(scope, { it.songId }) { api.readSongs() }
    val file = ItemCache(scope, { it }) { api.readUserFiles() }
    val galaxy = ItemCache(scope, { it.galaxyId }) { api.readGalaxies() }

    fun reset() {
        console.log("user signed out, resetting cache")
        talent.clear()
        song.clear()
        file.clear()
        galaxy.clear()
    }
}