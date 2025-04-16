package streetlight.app

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.News
import compose.icons.tablericons.YinYang
import pondui.ui.core.PondConfig
import pondui.ui.nav.PortalRoute
import pondui.ui.nav.defaultScreen
import kotlinx.collections.immutable.persistentListOf
import streetlight.app.ui.HelloScreen
import streetlight.app.ui.StartScreen

val appConfig = PondConfig(
    name = "Streetlight",
    logo = TablerIcons.News,
    home = StartRoute,
    navGraph = {
        defaultScreen<StartRoute> { StartScreen(it) }
        defaultScreen<HelloRoute> { HelloScreen(it) }
    },
    portalItems = persistentListOf(
        PortalRoute(TablerIcons.YinYang, HelloRoute),
    )
)