package streetlight.model.data

import kampfire.api.EndpointParam
import kampfire.api.PathBuilder
import kampfire.model.GeoRect
import kotlin.uuid.Uuid

/** A request for the posts in [view], leaving out the areas already [seen], paged by [cursor]. */
data class MapQuery(
    val view: GeoRect,
    val seen: List<GeoRect>?,
    val cursor: EntityCursor.Lean
)

/** Writes [query] as the parameters of its endpoint. */
context(builder: PathBuilder)
fun <T: MapEndpoint> T.writeMapQuery(query: MapQuery) {
    builder.writeParam(view, query.view)
    builder.writeParam(seen, query.seen)
    if (query.cursor != EntityCursor.Lean.Default) {
        builder.writeParam(recordId, query.cursor.recordId)
        builder.writeParam(score, query.cursor.postLean)
    }
}

interface MapEndpoint {
    val view: EndpointParam<GeoRect?>
    val seen: EndpointParam<List<GeoRect>?>
    val recordId: EndpointParam<Uuid>
    val score: EndpointParam<Int>

    companion object {
        const val ViewParam = "view"
        const val SeenParam = "seen"
        const val RecordIdParam = "recordId"
        const val ScoreParam = "score"
    }
}