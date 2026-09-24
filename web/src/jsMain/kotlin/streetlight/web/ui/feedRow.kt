package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.div
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.web.layouts.EntityCell
import streetlight.web.layouts.toCells
import streetlight.web.layouts.configureFeedRow

/** Appends a feed row; see the server-rendered `feedRow`. */
fun AppendScope.feedRow(
    entity: Entity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
) = div(null) {
    configureFeedRow(entity, isUniverse, curator, cells)
}