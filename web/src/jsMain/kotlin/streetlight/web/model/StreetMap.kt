@file:OptIn(FlowPreview::class)

package streetlight.web.model

import koala.model.GeoMap
import koala.model.MapContextId
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import streetlight.model.data.ContentPost
import streetlight.model.data.Galaxy
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Post

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

    fun setPosts(posts: List<Post>?) {
        val posts = posts?.let { createEntities(posts) } ?: emptyList()
        geoMap.removeEntities(state.now.posts.map { it.entityId })
        geoMap.addEntities(posts)
        state.set { it.copy(posts = posts) }
    }

    fun addPosts(posts: List<Post>) {
        val posts = createEntities(posts)
        geoMap.addEntities(posts)
        state.set { it.copy(posts = it.posts + posts)}
    }

    private fun createEntities(posts: List<Post>): List<EventEntity> {
        return posts.mapNotNull { post ->
            val position = post.geoPoint ?: return@mapNotNull null
            val galaxy = post.galaxyId?.let { cache.topGalaxies.getCachedItem(it) }
            when (post) {
                is EventPost -> EventEntity(post, galaxy, position)
                is LocationPost -> return@mapNotNull null
                is ContentPost -> TODO()
            }
        }
    }
}

data class StreetMapState(
    val galaxies: List<Galaxy> = emptyList(),
    val posts: List<EventEntity> = emptyList(),
    val focus: MapFocus = MapFocus(),
)

data class MapFocus(
    val location: Location? = null,
    val event: EventLocation? = null,
)

const val streetMapId: MapContextId = "StreetMap"