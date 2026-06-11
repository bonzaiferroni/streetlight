package streetlight.web.ui

import koala.dom.DOMRender
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.textBlock
import streetlight.web.StarRoute

fun DOMRender.viewStarProfile(username: String) {
    column {
        textBlock("Profile of: $username")
    }
}

fun DOMRender.viewStarProfileRoute() {
    routeBlock<StarRoute, String>(portal, { route ->
//        val star = model.client.api.readStarByUsername(route.pathId) ?: return@routeBlock null
//        val listing = model.client.api.readStarPosts(route.pathId) ?: return@routeBlock null
//        GalaxyProfileContent(
//            galaxy = galaxy,
//            listing = listing,
//        )
        route.slug.string
    }) { content ->
        viewStarProfile(content)
    }
}