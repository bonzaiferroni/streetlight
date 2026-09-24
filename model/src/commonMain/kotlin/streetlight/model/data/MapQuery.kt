package streetlight.model.data

import kampfire.api.PathBuilder
import kampfire.model.GeoRect
import streetlight.model.Api

/** A request for the posts in [view], leaving out the areas already [seen], paged by [cursor]. */
data class MapQuery(
    val view: GeoRect,
    val seen: List<GeoRect>?,
    val cursor: EntityCursor.Lean
)

/** Writes [query] as the parameters of its endpoint. */
fun PathBuilder.writeMapQuery(query: MapQuery) {
    val it = Api.Posts.ReadMapQuery
    writeParam(it.view, query.view)
    writeParam(it.seen, query.seen)
    if (query.cursor != EntityCursor.Lean.Default) {
        writeParam(it.postId, query.cursor.recordId)
        writeParam(it.postLean, query.cursor.postLean)
    }
}