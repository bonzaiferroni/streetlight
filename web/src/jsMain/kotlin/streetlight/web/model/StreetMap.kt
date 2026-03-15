@file:OptIn(FlowPreview::class)

package streetlight.web.model

import koala.model.GeoMap
import koala.model.MapContextId
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import streetlight.model.data.Galaxy
import streetlight.model.data.EventInfo
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location

class StreetMap(
    private val scope: CoroutineScope,
    private val client: ClientContext,
    private val cache: UserCache,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow = state.now
    
    val transit = TransitMap(scope, client, geoMap)

    val communityFlow = stateFlow.mapDistinct { it.galaxies }

    fun setPosts(posts: List<GalaxyPost>) {
        val entities = posts.mapNotNull { post ->
            val galaxy = cache.galaxy.getCachedItem(post.galaxyId) ?: return@mapNotNull null
            val position = post.location?.geoPoint ?: return@mapNotNull null
            PostEntity(post, galaxy, position)
        }
        geoMap.removeEntities(state.now.posts.map { it.entityId })
        geoMap.addEntities(entities)
        state.set { it.copy(posts = entities) }
    }
}

data class StreetMapState(
    val galaxies: List<Galaxy> = emptyList(),
    val posts: List<PostEntity> = emptyList(),
    val focus: MapFocus = MapFocus(),
)

data class MapFocus(
    val location: Location? = null,
    val event: EventInfo? = null,
)

const val streetMapId: MapContextId = "StreetMap"