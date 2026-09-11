package streetlight.web.model

import koala.model.ItemCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventId
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.StarType
import streetlight.web.io.ApiClient
import streetlight.web.ui.StarLightKey

//class DataCache(
//    scope: CoroutineScope,
//    private val config: SiteConfig,
//    private val api: ApiClient,
//    private val gate: SessionGate,
//    private val toaster: Toaster,
//) {
//    init {
//        scope.launch {
//            gate.signedOutAtFlow.collect { signedOutAt ->
//                if (signedOutAt != null) {
//                    reset()
//                }
//            }
//        }
//    }
//
//    val talent = ItemCache(scope, toaster, { it.talentId }) { api.readTalents() }
//    val song = ItemCache(scope, toaster, { it.songId }) { api.readSongs() }
//    // val file = ItemCache(scope, { it }) { api.readUserFiles() }
//    val topGalaxies = ItemCache(scope, toaster, { it.galaxyId }) { api.readTopGalaxies() }
//
//    // val galaxy = GalaxyCache(scope, config, api)
//
//    val galaxyStars = StarCache(
//        starType = StarType.Galaxy,
//        cacheKey = StarLightKey.GALAXY_LIGHT_CACHE,
//        idToUuid = { it.value },
//        uuidToId = { GalaxyId(it) },
//        itemToId = { it.galaxyId },
//        starLinkEdit = { api.editStarLink(it) },
//        readRemoteLights = { api.readGalaxyStars() },
//        readRemoteItems = { api.readGalaxies(it) },
//        onError = toaster,
//        scope = scope,
//        gate = gate,
//    )
//
//    val eventStars = StarCache(
//        starType = StarType.Event,
//        cacheKey = StarLightKey.EVENT_LIGHT_CACHE,
//        idToUuid = { it.value },
//        uuidToId = { EventId(it) },
//        itemToId = { it.eventId },
//        starLinkEdit = { api.editStarLink(it) },
//        readRemoteLights = { api.readEventStars() },
//        readRemoteItems = { api.readEventLocations(it) },
//        onError = toaster,
//        scope = scope,
//        gate = gate,
//    )
//
//    val galaxy = ItemCache<Galaxy>()
//
//    fun reset() {
//        console.log("user signed out, resetting cache")
//        talent.clear()
//        song.clear()
//        // file.clear()
//        topGalaxies.clear()
//        // event.clear() temporary for debugging
//        // galaxy.clear()
//    }
//}