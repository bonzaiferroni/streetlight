package streetlight.web.view

import koala.SvgFile
import koala.dom.*
import koala.component.navigation
import koala.modifier.*
import kampfire.model.tapOf
import streetlight.web.model.StableDock
import streetlight.web.model.dockRoute
import streetlight.web.page.AppOverlay
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.model.dock
import streetlight.web.model.portal

fun ViewScope.wireRouteDock() {
    mountChildView(AppOverlay.RouteDockId) {
        viewRouteDock()
    }
}

/**
 * The dock over the bottom of the page: a pill of the routes of the dock shown, centered, with a tray at its left that
 * switches between the focus dock and the [StableDock] while the route has a focus. The cousin of the app's `RouteDock`.
 */
fun ViewScope.viewRouteDock() {
    val model = dock

    row(modify(RouteDockStyle.Container, TextUppercase, TextSmall, Gap(1), AlignItemsCenter, MarginBottom(2))) {
        flowBlock(model.focusDockState, modify(Flex1, Magic, DisplayFlex, JustifyContentEnd)) { focusDock ->
            focusDock ?: return@flowBlock
            flowBlock(model.isFocusShownState) { isFocusShown ->
                val icon = focusDock.icon.takeIf { isFocusShown } ?: SvgFile.Flame
                val glassMod = modify(RouteDockStyle.Glass, RouteDockStyle.Focus.takeIf { isFocusShown }, BlurBackdrop)
                row(modify(glassMod, BorderRadiusPill, BorderSolid2Px, Padding(1))) {
                    this@flowBlock.button(icon.small, model::toggleFocus, modify(SmallIconHeight, Aspect1))
                }
            }
        }
        flowBlock(model.shownDockState, Magic) { routes ->
            val isStable = routes == StableDock
            val glassMod = modify(RouteDockStyle.Glass, RouteDockStyle.Focus.takeIf { !isStable }, BlurBackdrop)
            row(modify(glassMod, BorderRadiusPill, Gap(0), Padding(1), Bold, AlignItemsCenter, BorderSolid2Px)) {
                routes.forEach { labeledRoute ->
                    val route = labeledRoute.route
                    val isRouteNow = when {
                        isStable -> model.stableRouteState.tapOf { it == route }
                        else -> portal.routeState.tapOf { it.dockRoute == route }
                    }
                    this@flowBlock.navigation(route, modify(BorderRadiusPill, PaddingY(1), PaddingX(2))) { +labeledRoute.label }
                        .asWeb().flowModifier(isRouteNow, RouteDockStyle.RouteNow, contentScope)
                }
            }
        }
        box(Flex1)
    }.flowModifier(model.isVisibleState.tapOf { !it }, RouteDockStyle.Hidden, contentScope)
}
