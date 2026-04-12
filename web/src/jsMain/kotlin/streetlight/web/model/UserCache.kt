package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventId
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.web.io.ApiClient
import streetlight.web.ui.StarLightKey

class UserCache(
    scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
    private val gate: StarGate,
) {
    init {
        scope.launch {
            gate.starFlow.collect { user ->
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

    // val galaxy = GalaxyCache(scope, config, api)

    val galaxyLights = LightCache(
        cacheKey = StarLightKey.GALAXY_LIGHT_CACHE,
        idToString = { it.value },
        stringToId = { GalaxyId(it) },
        lightEdit = { api.editGalaxyLight(it) },
        readRemoteLights = { api.readGalaxyLights()?.toSet() },
        readRemoteItems = { api.readGalaxies(it)?.sortedBy { galaxy -> galaxy.createdAt } },
        scope = scope,
        config = config,
    )

    val eventLights = LightCache(
        cacheKey = StarLightKey.EVENT_LIGHT_CACHE,
        idToString = { it.value },
        stringToId = { EventId(it) },
        lightEdit = { api.editEventLight(it) },
        readRemoteLights = { api.readEventLights()?.toSet() },
        readRemoteItems = { api.readEventLocations(it)?.sortedBy { event -> event.startsAt } },
        scope = scope,
        config = config,
    )

    val galaxy = ItemCache<Galaxy>()

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