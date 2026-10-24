package streetlight.web.subApp

import kampfire.api.toSlug
import kampfire.model.Ok
import kampfire.model.Outcome
import koala.model.WebRoute
import koala.model.ContentFetcher
import koala.model.FetcherContent
import koala.model.NullContent
import streetlight.model.ui.EventRoute
import streetlight.model.ui.LocationAppRoute
import streetlight.model.io.ApiClient
import web.window.window

/**
 * Fetches the content of each route of a LocationApp from the API, for the location at the subdomain of the page's
 * host, or [NullContent] for a route that needs none.
 */
class LocationAppFetcher(
    val api: ApiClient
): ContentFetcher {
    private val subdomain = window.location.hostname.substringBefore('.').toSlug()

    override suspend fun fetchContent(route: WebRoute): Outcome<FetcherContent> = when (route) {
        is LocationAppRoute -> api.location.readSubdomainContent(subdomain)
        is EventRoute -> api.event.readEventSlug(route.slug)
        else -> Ok(NullContent)
    }
}
