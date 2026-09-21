package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import koala.html.AppRoute
import koala.model.ContentFetcher
import koala.model.FetcherContent
import koala.model.NullContent
import streetlight.model.ui.EventRoute
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.EventUpdateRoute
import streetlight.model.ui.GalaxyConfigRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.CityListRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.InboxRoute
import streetlight.model.ui.LocationConfigRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.LocationUpdateRoute
import streetlight.model.ui.MediaForgeRoute
import streetlight.model.ui.MediaRoute
import streetlight.model.ui.MediaUpdateRoute
import streetlight.model.ui.SiteDocRoute
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarRoute
import streetlight.model.ui.StarConfigRoute
import streetlight.web.io.ApiClient

class AppContentFetcher(
    val api: ApiClient
): ContentFetcher {
    override suspend fun fetchContent(route: AppRoute): Outcome<FetcherContent> = when (route) {
        is HomeRoute -> api.content.readHomeContent()
        is CityListRoute -> api.content.readCityListContent()
        is GalaxyRoute -> api.galaxy.readGalaxyContent(route.slug)
        is GalaxyConfigRoute -> api.galaxy.readGalaxyConfig(route.slug)
        is LocationRoute -> api.location.readLocationContent(route.slug)
        is EventRoute -> api.event.readEventSlug(route.slug)
        is StarRoute -> api.star.readStarContent(route.username)
        is LocationScoutRoute -> route.slug?.let { api.galaxy.readGalaxy(it) } ?: Ok(NullContent)
        is LocationUpdateRoute -> api.location.readLocationUpdaterContent(route.slug)
        is EventScoutRoute -> route.slug?.let { api.galaxy.readGalaxy(it) } ?: Ok(NullContent)
        is LocationConfigRoute -> api.location.readLocationConfigContent(route.locationId)
        is EventUpdateRoute -> api.event.readEventUpdaterContent(route.slug)
        is MediaRoute -> api.media.readMedia(route.slug)
        is MediaUpdateRoute -> api.media.readMedia(route.slug)
        is MediaForgeRoute -> route.slug?.let { api.galaxy.readGalaxy(it) } ?: Ok(NullContent)
        is SiteDocRoute -> api.doc.readSiteDoc(route.docId)
        is StarDashRoute -> Ok(NullContent)
        is ProfileConfigRoute -> api.star.readProfileDesign()
        is StarConfigRoute -> api.star.readAccount()
        is InboxRoute -> api.message.readInbox()
        else -> Ok(NullContent)
    }
}

