package streetlight.web.dom.layout

import koala.dom.AppendScope
import koala.dom.layout.div
import koala.modifier.Modifier
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.web.layout.EntityCell
import streetlight.web.model.toCells
import streetlight.web.layout.configureFeedRow

/** Appends a feed row; see the server-rendered `feedRow`. */
fun AppendScope.feedRow(
    entity: Entity,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
    mod: Modifier? = null,
) = div(mod) {
    configureFeedRow(entity, curator, cells)
}