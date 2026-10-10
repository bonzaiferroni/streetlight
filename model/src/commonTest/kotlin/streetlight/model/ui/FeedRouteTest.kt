package streetlight.model.ui

import kampfire.api.Slug
import koala.html.AppRoute
import streetlight.model.data.FeedType
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedRouteTest {

    @Test
    fun `a home route with a feed reads back from its path`() {
        val route = HomeRoute(FeedType.Events)
        val (path, query) = route.toRelativePath().split('?')

        assertEquals(route, AppRoute.routeOf(path, query, Screen.entries))
    }

    @Test
    fun `a city route with a feed reads back from its path`() {
        val route = CityRoute(Slug("denver-colorado"), FeedType.Locations)
        val (path, query) = route.toRelativePath().split('?')

        assertEquals(route, AppRoute.routeOf(path, query, Screen.entries))
    }

    @Test
    fun `a home route showing its default feed has a bare path`() {
        assertEquals("/", HomeRoute().toRelativePath())
    }

    @Test
    fun `an unknown feed name reads as the default feed`() {
        assertEquals(HomeRoute(), AppRoute.routeOf("/", "feed=Nonsense", Screen.entries))
    }
}
