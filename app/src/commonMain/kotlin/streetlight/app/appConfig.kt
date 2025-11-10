package streetlight.app

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Location
import compose.icons.tablericons.Music
import compose.icons.tablericons.Tornado
import pondui.ui.core.PondConfig
import pondui.ui.nav.PortalDoor
import pondui.ui.nav.defaultScreen
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.core.RouteConfig
import streetlight.app.ui.StreetListScreen
import streetlight.app.ui.EventFeedScreen
import streetlight.app.ui.HelloScreen
import streetlight.app.ui.StreetProfileScreen
import streetlight.app.ui.LocationProfileScreen
import streetlight.app.ui.SongFeedScreen
import streetlight.app.ui.StartScreen
import streetlight.app.ui.EventProfileScreen
import streetlight.app.ui.SongProfileScreen

val appConfig = PondConfig(
    name = "Streetlight",
    logo = TablerIcons.Flame,
    home = EventFeedRoute,
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen(it) } },
        RouteConfig(HelloRoute::matchRoute) { defaultScreen<HelloRoute> { HelloScreen() } },
        RouteConfig(EventFeedRoute::matchRoute) { defaultScreen<EventFeedRoute> { EventFeedScreen() } },
        RouteConfig(StreetListRoute::matchRoute) { defaultScreen<StreetListRoute> { StreetListScreen() } },
        RouteConfig(StreetProfileRoute::matchRoute) { defaultScreen<StreetProfileRoute> { StreetProfileScreen(it) } },
        RouteConfig(SongFeedRoute::matchRoute) { defaultScreen<SongFeedRoute> { SongFeedScreen() } },
        RouteConfig(LocationProfileRoute::matchRoute) { defaultScreen<LocationProfileRoute> { LocationProfileScreen(it) } },
        RouteConfig(EventProfileRoute::matchRoute) { defaultScreen<EventProfileRoute> { EventProfileScreen(it) } },
        RouteConfig(SongProfileRoute::matchRoute) { defaultScreen<SongProfileRoute> { SongProfileScreen(it) } },
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Tornado, HelloRoute),
        PortalDoor(TablerIcons.CalendarEvent, EventFeedRoute),
        PortalDoor(TablerIcons.Location, StreetListRoute),
        PortalDoor(TablerIcons.Music, SongFeedRoute, requireLogin = true)
    ),
)