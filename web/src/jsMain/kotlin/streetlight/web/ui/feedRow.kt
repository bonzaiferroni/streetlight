package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.div
import streetlight.model.data.CuratorStatus
import streetlight.model.data.FeedEntity
import streetlight.web.layouts.EntityCell
import streetlight.web.layouts.cells
import streetlight.web.layouts.configureFeedRow

fun AppendScope.feedRow(
    entity: FeedEntity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cells: List<EntityCell> = entity.cells,
) = div(null) {
    configureFeedRow(entity, isUniverse, curator, cells)
}