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
import streetlight.app.ui.SongFeedScreen
import streetlight.app.ui.StartScreen
import streetlight.app.ui.EventProfileScreen

val appConfig = PondConfig(
    name = "Streetlight",
    logo = TablerIcons.TrafficLights,
    home = EventFeedRoute,
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen(it) } },
        RouteConfig(HelloRoute::matchRoute) { defaultScreen<HelloRoute> { HelloScreen(it) } },
        RouteConfig(EventFeedRoute::matchRoute) { defaultScreen<EventFeedRoute> { EventFeedScreen() } },
        RouteConfig(AreaListRoute::matchRoute) { defaultScreen<AreaListRoute> { AreaListScreen() } },
        RouteConfig(AreaProfileRoute::matchRoute) { defaultScreen<AreaProfileRoute> { AreaProfileScreen(it) } },
        RouteConfig(SongListRoute::matchRoute) { defaultScreen<SongListRoute> { SongFeedScreen() } },
        RouteConfig(LocationProfileRoute::matchRoute) { defaultScreen<LocationProfileRoute> { LocationProfileScreen(it) } },
        RouteConfig(EventProfileRoute::matchRoute) { defaultScreen<EventProfileRoute> { EventProfileScreen(it) } },
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.CalendarEvent, EventFeedRoute),
        PortalDoor(TablerIcons.Location, AreaListRoute),
        PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Music, SongListRoute, requireLogin = true)
    ),
)