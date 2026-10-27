@file:OptIn(FlowPreview::class)

package streetlight.compose.model

import kampfire.model.GeoRect
import kampfire.model.Ok
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.EntityCursor
import streetlight.model.data.EventTag
import streetlight.model.data.MapQuery
import streetlight.model.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads the events of each settled view of the map into [markerMap] while the map shows it, so a view already covered
 * by a completed query is not asked again. The events are limited to those carrying its tag and matching its search
 * when given, and a search first queries the whole earth. The web's `EarthCache`.
 */
class EarthCache(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geoMap: GeoMap,
    private val markerMap: MarkerMap,
) {
    private val queries = mutableMapOf<GeoRect, MapCursor>()
    private var tag: EventTag? = null
    private var search: String? = null
    // a query started before a restart is dropped when it lands
    private var generation = 0
    // the markers from before a restart stay until the first query after it lands
    private var isReplacing = false

    init {
        scope.launch {
            // a view settles, or the map comes to show these markers over the view it holds
            combine(geoMap.markerMapState.flow, geoMap.camera.cameraState.flow) { shown, camera ->
                camera.bounds?.takeIf { shown === markerMap && !camera.isMoving }
            }
                .distinctUntilChanged()
                .filterNotNull()
                .debounce(SettleDelay)
                .collect { view -> launch { query(view) } }
        }
    }

    /**
     * Starts over with the events carrying [tag] and matching [search], when given, querying the view the map holds. The
     * markers shown stay until the first query lands and takes their place, so a marker it holds again is kept.
     */
    fun restart(tag: EventTag?, search: String?) {
        this.tag = tag
        this.search = search
        generation++
        queries.clear()
        isReplacing = true
        val camera = geoMap.camera.cameraState.now
        val view = camera.bounds?.takeIf { geoMap.markerMapState.now === markerMap } ?: return
        scope.launch { query(view) }
    }

    /** Queries [view] unless it is covered, and adds its events to the map. */
    private suspend fun query(view: GeoRect) {
        val start = generation
        val query = getQuery(view) ?: return
        // held as incomplete while in flight, so an overlapping view at this zoom is not queried again
        queries[query.view] = MapCursor(null, false)
        // a failed read leaves the view to be asked again, as the view has no toaster yet
        val feed = (api.earth.readMapEntities(query) as? Ok)?.data ?: run {
            if (generation == start) queries.remove(query.view)
            return
        }
        if (generation != start) return
        queries[query.view] = MapCursor(feed.nextCursor as? EntityCursor.Score, feed.isCompleted)
        if (isReplacing) markerMap.setPoints(feed.entities) else markerMap.addPoints(feed.entities)
        isReplacing = false
    }

    /**
     * The query for [view], reaching past it by [Overscan], or `null` when [view] is already covered. The first query of
     * a search reaches the whole earth.
     */
    private fun getQuery(view: GeoRect): MapQuery? {
        if (isCovered(view)) return null
        val queriedRect = if (search != null && queries.isEmpty()) GeoRect.World else view.scaleBy(Overscan)
        val containing = queries.filterKeys { it.contains(queriedRect) }

        val seen = queries.keys
            .filter { it !in containing.keys && queries.getValue(it).isComplete && it.overlapArea(queriedRect) > 0.0 }
            .sortedByDescending { it.overlapArea(queriedRect) }
            .take(MaxViewed)

        val cursor = containing.values.mapNotNull { it.cursor }.minWithOrNull(compareBy(nullsLast()) { it.score })
            ?: EntityCursor.Score.Default.copy(tag = tag?.ordinal, search = search)
        return MapQuery(queriedRect, seen, cursor)
    }

    /** Whether the queries together cover [view], counting an incomplete one only when it was queried at this zoom or closer. */
    private fun isCovered(view: GeoRect): Boolean {
        val maxIncompleteWidth = view.width * Overscan * (1 + WidthTolerance)
        var uncovered = listOf(view)
        queries.forEach { (rect, cursor) ->
            if (!cursor.isComplete && rect.width > maxIncompleteWidth) return@forEach
            uncovered = uncovered.flatMap { it.subtract(rect) }
            if (uncovered.isEmpty()) return true
        }
        return false
    }
}

/** The point where the query of one view left off. */
data class MapCursor(
    val cursor: EntityCursor.Score?,
    val isComplete: Boolean,
)

/** The time a view rests before it is queried. */
private val SettleDelay = 500.milliseconds

/** The most overlapping views a query lists as already seen. */
private const val MaxViewed = 8

/** The factor by which a query reaches past its view, so small pans stay covered. */
private const val Overscan = 1.5f

/** The relative difference in width still read as the same zoom. */
private const val WidthTolerance = 1e-6
