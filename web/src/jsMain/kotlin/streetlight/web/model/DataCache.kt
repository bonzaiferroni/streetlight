package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.EventId
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.LightType
import streetlight.model.data.Post
import streetlight.web.io.ApiClient
import streetlight.web.ui.StarLightKey

class DataCache(
    scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
    private val gate: UserGate,
    private val toaster: Toaster,
) {
    init {
        scope.launch {
            gate.signedOutAtFlow.collect { signedOutAt ->
                if (signedOutAt != null) {
                    reset()
                }
            }
        }
    }

    val talent = ItemCache(scope, toaster::toast, { it.talentId }) { api.readTalents() }
    val song = ItemCache(scope, toaster::toast, { it.songId }) { api.readSongs() }
    // val file = ItemCache(scope, { it }) { api.readUserFiles() }
    val topGalaxies = ItemCache(scope, toaster::toast, { it.galaxyId }) { api.readTopGalaxies() }

    val newPosts = MutableSharedFlow<Post>()

    // val galaxy = GalaxyCache(scope, config, api)

    val galaxyLights = LightCache(
        lightType = LightType.Galaxy,
        cacheKey = StarLightKey.GALAXY_LIGHT_CACHE,
        idToUuid = { it.value },
        uuidToId = { GalaxyId(it) },
        itemToId = { it.galaxyId },
        lightEdit = { api.editLight(it) },
        readRemoteLights = { api.readGalaxyLights() },
        readRemoteItems = { api.readGalaxies(it) },
        onError = toaster::toast,
        scope = scope,
        gate = gate,
    )

    val eventLights = LightCache(
        lightType = LightType.Event,
        cacheKey = StarLightKey.EVENT_LIGHT_CACHE,
        idToUuid = { it.value },
        uuidToId = { EventId(it) },
        itemToId = { it.eventId },
        lightEdit = { api.editLight(it) },
        readRemoteLights = { api.readEventLights() },
        readRemoteItems = { api.readEventLocations(it) },
        onError = toaster::toast,
        scope = scope,
        gate = gate,
    )

    val galaxy = ItemCache<Galaxy>()

    fun reset() {
        console.log("user signed out, resetting cache")
        talent.clear()
        song.clear()
        // file.clear()
        topGalaxies.clear()
        // event.clear() temporary for debugging
        // galaxy.clear()
    }
}