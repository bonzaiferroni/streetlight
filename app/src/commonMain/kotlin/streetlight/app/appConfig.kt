package streetlight.app

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Location
import compose.icons.tablericons.Music
import compose.icons.tablericons.TrafficLights
import compose.icons.tablericons.YinYang
import pondui.ui.core.PondConfig
import pondui.ui.nav.PortalRoute
import pondui.ui.nav.defaultScreen
import kotlinx.collections.immutable.persistentListOf
import streetlight.app.ui.AreaListScreen
import streetlight.app.ui.EventFeedScreen
import streetlight.app.ui.HelloScreen
import streetlight.app.ui.AreaProfileScreen
import streetlight.app.ui.LocationProfileScreen
import streetlight.app.ui.SongListScreen
import streetlight.app.ui.StartScreen

val appConfig = PondConfig(
    name = "Streetlight",
    logo = TablerIcons.TrafficLights,
    home = EventFeedRoute,
    navGraph = {
        defaultScreen<StartRoute> { StartScreen(it) }
        defaultScreen<HelloRoute> { HelloScreen(it) }
        defaultScreen<EventFeedRoute> { EventFeedScreen(it) }
        defaultScreen<AreaListRoute> { AreaListScreen(it) }
        defaultScreen<AreaProfileRoute> { AreaProfileScreen(it) }
        defaultScreen<SongListRoute> { SongListScreen(it) }
        defaultScreen<LocationProfileRoute> { LocationProfileScreen(it) }
    },
    portalItems = persistentListOf(
        PortalRoute(TablerIcons.CalendarEvent, EventFeedRoute),
        PortalRoute(TablerIcons.Location, AreaListRoute),
        PortalRoute(TablerIcons.YinYang, HelloRoute),
        PortalRoute(TablerIcons.Music, SongListRoute, requireLogin = true)
    )
)