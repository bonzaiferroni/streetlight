package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.textBlock
import streetlight.web.StarRoute

fun RenderContext.viewStarProfile(username: String) {
    column {
        textBlock("Profile of: $username")
    }
}

fun RenderContext.viewStarProfileRoute() {
    routeBlock<StarRoute, String>(portal, { route ->
//        val star = model.client.api.readStarByUsername(route.pathId) ?: return@routeBlock null
//        val listing = model.client.api.readStarPosts(route.pathId) ?: return@routeBlock null
//        GalaxyProfileContent(
//            galaxy = galaxy,
//            listing = listing,
//        )
        route.slug
    }) { content ->
        viewStarProfile(content)
    }
}