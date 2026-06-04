package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.BasicPost
import streetlight.model.data.EventPost
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LocationPost

class MapEntityService() {
    fun createEntities(posts: List<GalaxyPost>): List<AppEntity> {
        return posts.mapNotNull { post ->
            when (post) {
                is EventPost -> EventEntity(post)
                is LocationPost -> return@mapNotNull null
                is BasicPost -> return@mapNotNull null
            }
        }
    }
}