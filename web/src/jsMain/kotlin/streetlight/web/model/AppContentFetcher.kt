package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import koala.html.AppRoute
import koala.model.ContentFetcher
import koala.model.NullFetcherContent
import koala.model.RouteContent
import streetlight.model.ui.EventRoute
import streetlight.model.ui.GalaxyConfigRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.LocationUpdateRoute
import streetlight.model.ui.MediaUpdateRoute
import streetlight.model.ui.StarRoute
import streetlight.web.io.ApiClient
import streetlight.web.ui.api

class AppContentFetcher(
    val api: ApiClient
): ContentFetcher {
    override suspend fun fetchContent(route: AppRoute): Outcome<RouteContent> = when (route) {
        is HomeRoute -> api.readHomeContent()
        is MediaUpdateRoute -> api.readMedia(route.slug)

        is GalaxyRoute -> api.readGalaxyContent(route.slug)
        is GalaxyConfigRoute -> api.readGalaxy(route.slug)
        is LocationRoute -> api.readLocationContent(route.slug)
        is EventRoute -> api.readEventSlug(route.slug)
        is StarRoute -> api.readStarContent(route.username)
        is LocationScoutRoute -> api.readGalaxy(route.slug)
        is LocationUpdateRoute -> api.readLocationUpdaterContent(route.slug)
        else -> Ok(NullFetcherContent)
    }
}

