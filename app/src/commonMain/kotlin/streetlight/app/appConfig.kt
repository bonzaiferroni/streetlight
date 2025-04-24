package streetlight.app

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Location
import compose.icons.tablericons.Music
import compose.icons.tablericons.TrafficLights
import compose.icons.tablericons.YinYang
import pondui.ui.core.PondConfig
import pondui.ui.nav.PortalDoor
import pondui.ui.nav.defaultScreen
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.core.RouteConfig
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
    doors = persistentListOf(
        PortalDoor(TablerIcons.CalendarEvent, EventFeedRoute),
        PortalDoor(TablerIcons.Location, AreaListRoute),
        PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Music, SongListRoute, requireLogin = true)
    ),
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen(it) } },
        RouteConfig(HelloRoute::matchRoute) { defaultScreen<HelloRoute> { HelloScreen(it) } },
        RouteConfig(EventFeedRoute::matchRoute) { defaultScreen<EventFeedRoute> { EventFeedScreen(it) } },
        RouteConfig(AreaListRoute::matchRoute) { defaultScreen<AreaListRoute> { AreaListScreen(it) } },
        RouteConfig() { defaultScreen<AreaProfileRoute> { AreaProfileScreen(it) } },
        RouteConfig(SongListRoute::matchRoute) { defaultScreen<SongListRoute> { SongListScreen(it) } },
        RouteConfig() { defaultScreen<LocationProfileRoute> { LocationProfileScreen(it) } },
    )
)