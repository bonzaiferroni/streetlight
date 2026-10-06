@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoRect
import kampfire.model.toDataOr
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import streetlight.model.data.MapQuery
import streetlight.model.data.EntityCursor
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

/** Loads the events of each settled map view, so a view already covered by a completed query is not asked again. */
class EarthCache(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val markerMap: MarkerMap,
    private val toaster: Toaster,
) {
    var isQueriedMap = false

    private val queries = mutableMapOf<GeoRect, MapCursor>()
    private val camera get() = markerMap.geoMap.camera

    init {
        scope.launch("Earth > event query") {
            camera.settledViewState.flow.debounce(500.milliseconds).collect {
                queryEvents(it)
            }
        }
    }

    /** Starts over for a new map; only a map with [isQueriedMap] loads events by view. */
    fun setMapContext(isQueriedMap: Boolean) {
        this.isQueriedMap = isQueriedMap
        queries.clear()
        // queryPosts()
    }

    private fun queryPosts(view: GeoRect? = null) {
        if (!isQueriedMap) return
        scope.launch {
            println("querying map")
            val queriedView = view ?: camera.viewedState.flow.first { !it.isMoving }.view
            val query = getQuery(queriedView) ?: return@launch
            val feed = api.post.readMapPosts(query).toDataOr(toaster) { return@launch }
            queries[queriedView] = MapCursor(feed.nextCursor as? EntityCursor.Score, feed.isCompleted)
            markerMap.addPoints(feed.entities)
        }
    }

    private fun queryEvents(view: GeoRect? = null) {
        if (!isQueriedMap) return
        scope.launch {
            val queriedView = view ?: camera.viewedState.flow.first { !it.isMoving }.view
            val query = getQuery(queriedView) ?: return@launch
            val feed = api.earth.readMapEntities(query).toDataOr(toaster) { return@launch }
            queries[queriedView] = MapCursor(feed.nextCursor as? EntityCursor.Score, feed.isCompleted)
            markerMap.addPoints(feed.entities)
        }
    }

    private fun getQuery(view: GeoRect): MapQuery? {
        val containing = queries.filterKeys { it.contains(view) }
        if (containing.values.any { it.isComplete }) return null

        val seen = queries.keys
            .filter { it !in containing.keys && queries.getValue(it).isComplete && it.overlapArea(view) > 0.0 }
            .sortedByDescending { it.overlapArea(view) }
            .take(MAX_VIEWED)

        val query = containing.values.mapNotNull { it.cursor }.minWithOrNull(compareBy(nullsLast()) { it.score })
            ?: EntityCursor.Score.Default
        return MapQuery(view, seen, query)
    }
}

/** Where the query of one view left off. */
data class MapCursor(
    val cursor: EntityCursor.Score?,
    val isComplete: Boolean
)

/** The most overlapping views a query lists as already seen. */
const val MAX_VIEWED = 8