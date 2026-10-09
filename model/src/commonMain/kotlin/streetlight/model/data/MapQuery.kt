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
    tagParam?.let { param -> query.cursor.tag?.let { builder.writeParam(param, it) } }
    query.cursor.recordId?.let {
        builder.writeParam(recordIdParam, it)
        builder.writeParam(scoreParam, query.cursor.score)
    }
}

/** An endpoint that pages the entities of a map view, filtered by tag when it has a [tagParam]. */
interface MapEndpoint {
    val viewParam: EndpointParam<GeoRect?>
    val seenParam: EndpointParam<List<GeoRect>?>
    val recordIdParam: EndpointParam<Uuid>
    val scoreParam: EndpointParam<Double>
    val tagParam: EndpointParam<Int>? get() = null

    companion object {
        const val ViewParam = "view"
        const val SeenParam = "seen"
        const val RecordIdParam = "recordId"
        const val ScoreParam = "score"
        const val TagParam = "tag"
    }
}