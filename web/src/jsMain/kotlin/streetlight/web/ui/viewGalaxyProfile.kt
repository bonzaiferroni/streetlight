package streetlight.web.ui

import koala.core.queryFirstOrNull
import koala.css.StyleProperty
import koala.css.UrlValue
import koala.dom.*
import koala.html.IconElement
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventStar
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
    val config = app.config

    val root = shellBox(GalaxyShell.galaxyBoxId, app.geoMap, app.appScope) {
        galaxyShell(content)
    }

    root.onFirstView {
        wireBlock(GalaxyShell.mapPanelId) {
            val element = column {
                row {
                    switch("show transit", onToggle = config::setShowTransit, bindFlow = config.showTransitFlow)
                }
                content.posts.forEach { post ->
                    gridOf(post)
                }
            }
            wireInterestControls(app, element)
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
    root.wireByAttribute<EventStar>(EventAttributes.interest) { element, interest ->
        var interest = interest

        element.onClick {
            interest = interest.copy(
                value = if (interest.value == null) InterestType.Star else null
            )

            app.userInterest.editEventInterest(interest)
        }

        val iconElement by lazy { element.queryFirstOrNull(IconElement.cssClass) }
        renderScope.launch {
            app.userInterest.interestFlow.collect { updatedInterest ->
                if (updatedInterest.eventId != interest.eventId) return@collect
                interest = updatedInterest

                val element = iconElement ?: return@collect
                element.style.setProperty(StyleProperty.maskUrl, UrlValue(interest.value.iconPath))
            }
        }
    }
}