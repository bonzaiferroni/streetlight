package streetlight.app.utils

import pondui.ui.nav.IdRoute
import streetlight.app.StreetProfileRoute
import streetlight.app.EventProfileRoute
import streetlight.app.LocationProfileRoute
import streetlight.app.SongProfileRoute
import streetlight.model.data.CommunityId
import streetlight.model.data.EventId
import streetlight.model.data.LocationId
import streetlight.model.data.ProjectId
import streetlight.model.data.SongId

inline fun <reified T: IdRoute<String>> ProjectId.toRoute(): T = when (this) {
    is CommunityId -> StreetProfileRoute(value) as T
    is EventId -> EventProfileRoute(value) as T
    is LocationId -> LocationProfileRoute(value) as T
    is SongId -> SongProfileRoute(value) as T
    else -> error("no route for ${this::class.simpleName}")
}