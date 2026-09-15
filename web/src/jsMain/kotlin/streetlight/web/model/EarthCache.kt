@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoRect
import kampfire.model.toDataOr
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.MapQuery
import streetlight.model.data.PostCursor
import streetlight.model.data.SortDirection
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

class EarthCache(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val markerMap: MarkerMap,
    private val toaster: Toaster,
) {
    var isQueriedMap = false

    private val queries = mutableMapOf<GeoRect, MapCursor>()

    init {
        scope.launch("Earth > post query") {
            markerMap.geoMap.camera.settledView.flow.debounce(500.milliseconds).collect {
                queryView(it)
            }
        }
    }

    fun setMapContext(isQueriedMap: Boolean) {
        this.isQueriedMap = isQueriedMap
        queries.clear()
    }

    private fun queryView(view: GeoRect) {
        if (!isQueriedMap) return
        scope.launch {
            val query = getQuery(view) ?: return@launch
            val feed = api.readMapPosts(query).toDataOr(toaster) { return@launch }
            println("entities: ${feed.entities.size}")
            println("nextCursor: ${feed.nextCursor} (${feed.nextCursor?.let { it::class.simpleName }})")
            println("completed: ${feed.isCompleted}")
            queries[view] = MapCursor(feed.nextCursor as? PostCursor.Lean, feed.isCompleted)
            markerMap.addPoints(feed.entities)
        }
    }

    private fun getQuery(view: GeoRect): MapQuery? {
        val containing = queries.filterKeys { it.contains(view) }
        if (containing.values.any { it.isComplete }) return null

        val seen = queries.keys
            .filter { it !in containing.keys && it.overlapArea(view) > 0.0 }
            .sortedByDescending { it.overlapArea(view) }
            .take(MAX_VIEWED)

        val query = containing.values.mapNotNull { it.cursor }.minWithOrNull(compareBy(nullsLast()) { it.postLean })
            ?: PostCursor.Lean.Default
        return MapQuery(view, seen, query)
    }
}

data class MapCursor(
    val cursor: PostCursor.Lean?,
    val isComplete: Boolean
)

const val MAX_VIEWED = 8