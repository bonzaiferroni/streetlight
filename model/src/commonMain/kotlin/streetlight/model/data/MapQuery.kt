package streetlight.model.data

import kampfire.api.EndpointParam
import kampfire.api.PathBuilder
import kampfire.model.GeoRect
import kotlin.uuid.Uuid

/** A request for the posts in [view], leaving out the areas already [seen], paged by [cursor]. */
data class MapQuery(
    val view: GeoRect,
    val seen: List<GeoRect>?,
    val cursor: EntityCursor.Score
)

/** Writes [query] as the parameters of its endpoint. */
context(builder: PathBuilder)
fun <T: MapEndpoint> T.writeMapQuery(query: MapQuery) {
    builder.writeParam(viewParam, query.view)
    builder.writeParam(seenParam, query.seen)
    if (query.cursor != EntityCursor.Score.Default) {
        builder.writeParam(recordIdParam, query.cursor.recordId)
        builder.writeParam(scoreParam, query.cursor.score)
    }
}

interface MapEndpoint {
    val viewParam: EndpointParam<GeoRect?>
    val seenParam: EndpointParam<List<GeoRect>?>
    val recordIdParam: EndpointParam<Uuid>
    val scoreParam: EndpointParam<Double>

    companion object {
        const val ViewParam = "view"
        const val SeenParam = "seen"
        const val RecordIdParam = "recordId"
        const val ScoreParam = "score"
    }
}