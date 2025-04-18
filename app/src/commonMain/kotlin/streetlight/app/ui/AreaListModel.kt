package streetlight.app.ui

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.core.StateModel
import streetlight.app.AreaListRoute
import streetlight.app.io.AreaStore
import streetlight.model.data.Area

class AreaListModel(
    route: AreaListRoute,
    store: AreaStore = AreaStore()
): StateModel<AreaListState>(AreaListState()) {
}

data class AreaListState(
    val areas: ImmutableList<Area> = persistentListOf()
)