package streetlight.model.data

import kampfire.api.PathBuilder
import kampfire.model.GeoRect
import streetlight.model.Api

data class MapQuery(
    val view: GeoRect,
    val seen: List<GeoRect>?,
    val cursor: EntityCursor.Lean
)

fun PathBuilder.writeMapQuery(query: MapQuery) {
    val it = Api.Posts.ReadMapQuery
    writeParam(it.view, query.view)
    writeParam(it.seen, query.seen)
    if (query.cursor != EntityCursor.Lean.Default) {
        writeParam(it.postId, query.cursor.recordId)
        writeParam(it.postLean, query.cursor.postLean)
    }
}