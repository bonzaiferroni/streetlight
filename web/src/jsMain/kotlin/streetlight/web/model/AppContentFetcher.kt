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
import streetlight.model.ui.UpdateProfileRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarRoute
import streetlight.model.ui.UpdateAccountRoute
import streetlight.web.io.ApiClient

class AppContentFetcher(
    val api: ApiClient
): ContentFetcher {
    override suspend fun fetchContent(route: AppRoute): Outcome<FetcherContent> = when (route) {
        is HomeRoute -> api.readHomeContent()
        is GalaxyRoute -> api.readGalaxyContent(route.slug)
        is GalaxyConfigRoute -> api.readGalaxy(route.slug)
        is LocationRoute -> api.readLocationContent(route.slug)
        is EventRoute -> api.readEventSlug(route.slug)
        is StarRoute -> api.readStarContent(route.username)
        is LocationScoutRoute -> route.slug?.let { api.readGalaxy(it) } ?: Ok(NullContent)
        is LocationUpdateRoute -> api.readLocationUpdaterContent(route.slug)
        is EventScoutRoute -> route.slug?.let { api.readGalaxy(it) } ?: Ok(NullContent)
        is LocationConfigRoute -> api.readLocationConfigContent(route.locationId)
        is EventUpdateRoute -> api.readEventUpdaterContent(route.slug)
        is MediaRoute -> api.readMedia(route.slug)
        is MediaUpdateRoute -> api.readMedia(route.slug)
        is MediaForgeRoute -> route.slug?.let { api.readGalaxy(it) } ?: Ok(NullContent)
        is SiteDocRoute -> api.readSiteDoc(route.docId)
        is StarDashRoute -> Ok(NullContent)
        is UpdateProfileRoute -> Ok(NullContent)
        is UpdateAccountRoute -> api.readAccount()
        is InboxRoute -> api.readInbox()
        else -> Ok(NullContent)
    }
}

