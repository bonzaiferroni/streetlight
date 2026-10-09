@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoRect
import kampfire.model.storeOf
import kampfire.model.toDataOr
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import streetlight.model.data.MapQuery
import streetlight.model.data.EntityCursor
import streetlight.model.data.EventTag
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
    private var tag: EventTag? = null
    private var searchText: String? = null

    private val queries = mutableMapOf<GeoRect, MapCursor>()
    private val camera get() = markerMap.geoMap.camera

    val isQueryingState = storeOf(false)

    init {
        scope.launch("Earth > event query") {
            camera.settledViewState.flow.debounce(500.milliseconds).collect {
                queryEvents(it)
            }
        }
    }

    /**
     * Starts over for a new map, returning once its first query has landed; only a map with [isQueriedMap] loads
     * events by view.
     *
     * Its events are limited to those carrying [tag] and matching [searchText] when given. A search first queries the
     * whole earth, then each view it has not covered.
     */
    suspend fun setMapContext(isQueriedMap: Boolean, tag: EventTag? = null, searchText: String? = null) {
        this.isQueriedMap = isQueriedMap
        this.tag = tag
        this.searchText = searchText
        queries.clear()
        if (isQueriedMap) query()
    }

    private fun queryPosts(view: GeoRect? = null) {
        if (!isQueriedMap) return
        scope.launch {
            println("querying map")
            val queriedView = view ?: camera.viewedState.flow.first { !it.isMoving }.view
            val query = getQuery(queriedView) ?: return@launch
            val feed = api.post.readMapPosts(query).toDataOr(toaster) { return@launch }
            queries[query.view] = MapCursor(feed.nextCursor as? EntityCursor.Score, feed.isCompleted)
            markerMap.addPoints(feed.entities)
        }
    }

    private fun queryEvents(view: GeoRect) {
        if (!isQueriedMap) return
        scope.launch { query(view) }
    }

    /** Queries [view], or the first settled view without one, and adds its events to the map. */
    private suspend fun query(view: GeoRect? = null) {
        val queriedView = view ?: camera.viewedState.flow.first { !it.isMoving }.view
        val query = getQuery(queriedView) ?: return
        // held as incomplete while in flight, so an overlapping view at this zoom is not queried again
        queries[query.view] = MapCursor(null, false)
        isQueryingState.set { true }
        val feed = api.earth.readMapEntities(query).toDataOr(toaster) {
            queries.remove(query.view)
            isQueryingState.set { false }
            return
        }
        queries[query.view] = MapCursor(feed.nextCursor as? EntityCursor.Score, feed.isCompleted)
        markerMap.addPoints(feed.entities)
        isQueryingState.set { false }
    }

    /**
     * The query for [view], reaching past it by [OVERSCAN], or `null` when [view] is already covered.
     *
     * The first query of a search reaches the whole earth.
     */
    private fun getQuery(view: GeoRect): MapQuery? {
        if (isCovered(view)) return null
        val queriedRect = if (searchText != null && queries.isEmpty()) GeoRect.World else view.scaleBy(OVERSCAN)
        val containing = queries.filterKeys { it.contains(queriedRect) }

        val seen = queries.keys
            .filter { it !in containing.keys && queries.getValue(it).isComplete && it.overlapArea(queriedRect) > 0.0 }
            .sortedByDescending { it.overlapArea(queriedRect) }
            .take(MAX_VIEWED)

        val query = containing.values.mapNotNull { it.cursor }.minWithOrNull(compareBy(nullsLast()) { it.score })
            ?: EntityCursor.Score.Default.copy(tag = tag?.ordinal, search = searchText)
        return MapQuery(queriedRect, seen, query)
    }

    /** Whether the queries together cover [view], counting an incomplete one only when it was queried at this zoom or closer. */
    private fun isCovered(view: GeoRect): Boolean {
        val maxIncompleteWidth = view.width * OVERSCAN * (1 + WIDTH_TOLERANCE)
        var uncovered = listOf(view)
        queries.forEach { (rect, cursor) ->
            if (!cursor.isComplete && rect.width > maxIncompleteWidth) return@forEach
            uncovered = uncovered.flatMap { it.subtract(rect) }
            if (uncovered.isEmpty()) return true
        }
        return false
    }
}

/** Where the query of one view left off. */
data class MapCursor(
    val cursor: EntityCursor.Score?,
    val isComplete: Boolean,
)

/** The most overlapping views a query lists as already seen. */
const val MAX_VIEWED = 8

/** The factor by which a query reaches past its view, so small pans stay covered. */
private const val OVERSCAN = 1.5f

/** The relative difference in width still read as the same zoom. */
private const val WIDTH_TOLERANCE = 1e-6