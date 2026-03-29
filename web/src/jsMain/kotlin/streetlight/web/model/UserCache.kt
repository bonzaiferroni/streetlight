package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient

class UserCache(
    scope: CoroutineScope,
    private val config: SiteConfig,
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
    @Deprecated("use GalaxyCache")
    val topGalaxies = ItemCache(scope, { it.galaxyId }) { api.readTopGalaxies() }
    val event = EventCache(scope, config, api)
    val galaxy = GalaxyCache(scope, config, api)

    fun reset() {
        console.log("user signed out, resetting cache")
        talent.clear()
        song.clear()
        file.clear()
        topGalaxies.clear()
        // event.clear() temporary for debugging
        // galaxy.clear()
    }
}