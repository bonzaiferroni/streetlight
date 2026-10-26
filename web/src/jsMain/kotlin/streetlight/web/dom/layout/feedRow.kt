package streetlight.web.dom.layout

import koala.dom.AppendScope
import koala.dom.component.div
import koala.modifier.Modifier
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.model.data.EntityDetail
import streetlight.model.data.toDetails
import streetlight.web.layout.configureFeedRow

/** Appends a feed row; see the server-rendered `feedRow`. */
fun AppendScope.feedRow(
    entity: Entity,
    curator: CuratorStatus? = null,
    cells: List<EntityDetail>? = entity.toDetails(),
    mod: Modifier? = null,
) = div(mod) {
    configureFeedRow(entity, curator, cells)
}