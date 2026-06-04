package streetlight.web.model

import streetlight.model.data.BasicPost
import streetlight.model.data.EventPost
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LocationPost

class MarkerService() {
    fun createEntities(posts: List<GalaxyPost>): List<AppMarker> {
        return posts.mapNotNull { post ->
            when (post) {
                is EventPost -> EventMarker(post)
                is LocationPost -> return@mapNotNull null
                is BasicPost -> return@mapNotNull null
            }
        }
    }
}