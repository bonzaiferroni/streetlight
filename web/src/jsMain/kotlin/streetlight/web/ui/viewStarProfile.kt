package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.textBlock
import koala.dom.viewContextOf
import streetlight.web.StarProfileRoute
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.viewStarProfile(username: String) {
    column {
        textBlock("Profile of: $username")
    }
}

fun ViewContext<Streetlight>.viewStarProfileRoute() {
    routeBlock<StarProfileRoute, String>(model.portal, { route ->
//        val star = model.client.api.readStarByUsername(route.pathId) ?: return@routeBlock null
//        val listing = model.client.api.readStarPosts(route.pathId) ?: return@routeBlock null
//        GalaxyProfileContent(
//            galaxy = galaxy,
//            listing = listing,
//        )
        route.slug
    }) { content ->
        viewContextOf(model) {
            viewStarProfile(content)
        }
    }
}