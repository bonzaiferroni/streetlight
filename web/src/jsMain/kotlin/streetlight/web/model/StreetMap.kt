@file:OptIn(FlowPreview::class)

package streetlight.web.model

import koala.model.GeoMap
import koala.model.MapContextId
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
    private val config: SiteConfig,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow = state.now
    
    val transit = TransitMap(scope, client, geoMap, config)

    fun setPosts(posts: List<GalaxyPost>) {
        val posts = createEntities(posts)
        geoMap.removeEntities(state.now.posts.map { it.entityId })
        geoMap.addEntities(posts)
        state.set { it.copy(posts = posts) }
    }

    fun addPosts(posts: List<GalaxyPost>) {
        val posts = createEntities(posts)
        geoMap.addEntities(posts)
        state.set { it.copy(posts = it.posts + posts)}
    }

    private fun createEntities(posts: List<GalaxyPost>): List<PostEntity> {
        return posts.mapNotNull { post ->
            val position = post.position ?: return@mapNotNull null
            val galaxy = cache.galaxy.getCachedItem(post.galaxyId)
            PostEntity(post, galaxy, position)
        }
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