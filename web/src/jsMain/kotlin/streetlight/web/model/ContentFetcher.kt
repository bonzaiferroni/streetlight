package streetlight.web.model

import kampfire.model.Outcome
import koala.html.AppRoute
import streetlight.model.data.StreetlightContent
import streetlight.model.ui.GalaxyRoute
import streetlight.web.io.ApiClient

class ContentFetcher(
    private val api: ApiClient
) {
    suspend fun fetchContent(route: AppRoute): Outcome<StreetlightContent>? = when (route) {
        is GalaxyRoute -> api.readGalaxyContent(route.slug)
        else -> null
    }
}