package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.div
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.FeedEntity
import streetlight.web.layouts.configureFeedRow

fun AppendScope.feedRow(
    entity: FeedEntity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cellContent: (FlowContent.() -> Unit)? = null
) = div(null) {
    configureFeedRow(entity, isUniverse, curator, cellContent)
}