package streetlight.web.ui

import koala.core.queryFirstOrNull
import koala.css.ElementClass
import koala.dom.*
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventInterest
import streetlight.model.data.InterestType
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.EventAttributes
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.GalaxyShellContent
import streetlight.web.shells.galaxyShell
import streetlight.web.shells.gridOf
import streetlight.web.shells.iconPath

fun RenderContext.viewGalaxyProfile(app: Streetlight, content: GalaxyShellContent) {
    val root = shellBox(GalaxyShell.galaxyBoxId, app.geoMap, app.appScope) {
        galaxyShell(content)
    }

    root.onView {
        wireBlock(GalaxyShell.mapPanelId) {
            column {
                content.posts.forEach { post ->
                    gridOf(post)
                }
            }
        }
    }

    wireInterestControls(app, root)

    app.streetMap.setPosts(content.posts)
}

fun ViewContext<Streetlight>.viewGalaxyProfileRoute() {
    routeBlock<GalaxyPathIdRoute, GalaxyShellContent>(model.portal, { route ->
        val galaxy = model.client.api.readGalaxy(route.pathId) ?: return@routeBlock null
        val posts = model.client.api.readPosts(galaxy.galaxyId) ?: return@routeBlock null
        GalaxyShellContent(
            galaxy = galaxy,
            posts = posts
        )
    }) { content ->
        viewGalaxyProfile(model, content)
    }
}

fun RenderContext.wireInterestControls(app: Streetlight, root: HTMLElement) {
    root.wireByAttribute<EventInterest>(EventAttributes.interest) { element, interest ->
        var interest = interest

        element.onClick {
            interest = interest.copy(
                value = if (interest.value == null) InterestType.Star else null
            )
            renderScope.launch {
                val isSuccess = app.client.api.editEventInterest(interest) ?: return@launch
                if (isSuccess) {
                    val iconElement = element.queryFirstOrNull(ElementClass.icon) ?: return@launch
                    iconElement.style.setProperty("--mask-src", "url('${interest.value.iconPath}')")
                }
            }
        }
    }
}