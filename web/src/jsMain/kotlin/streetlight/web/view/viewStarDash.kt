package streetlight.web.view

import koala.model.LabeledRoute
import streetlight.model.ui.StarRoute
import streetlight.web.model.RouteDockState
import koala.modifier.*
import koala.dom.*
import koala.component.btn
import koala.model.NullContent
import streetlight.model.data.Star
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.GalaxyFeedRoute
import streetlight.model.ui.SandboxIndexRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarDashTab
import streetlight.web.model.SessionClient
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.configBody
import streetlight.web.model.dock
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.portal

context(star: Star, route: StarDashRoute)
fun ViewScope.viewStarDash() {
    configBody("dash:", star.username.value, "viewStarDash.kt") {
        lazyTabs(paramKey = StarDashRoute.tabParam.key) {
            StarDashTab.entries.forEach {
                tab(it.label, it.svg) {
                    when (it) {
                        StarDashTab.Overview -> starOverview()
                        StarDashTab.ECommerce -> sellerDash()
                        StarDashTab.Sandbox -> sandboxContent()
                    }
                }
            }
        }
    }

    dock.mergeState(route, RouteDockState(listOf(LabeledRoute(StarRoute(star.username))), title = star.username.value))
}

fun RouteScope.viewStarDashRoute() {
    routeBlock<StarDashRoute, NullContent>() {
        starGate {
            viewStarDash()
        }
    }
}

context(star: Star)
private fun ViewScope.sandboxContent() {
    // val model = UserHub(renderScope, app.client.api)
    val gate = app.get<SessionClient>()

    column {
        row {
            textBlock("Hello ${star.username}!", Flex1)
            button("go home", onClick = { portal.go(HomeRoute()) })
            button("sign out", onClick = gate::signOut)
        }

        card {
            row {
                textBlock("Add things to the map.", Flex1)
                btn("Edit Profile", ProfileConfigRoute)
                btn("Go to sandbox", SandboxIndexRoute)
                button("galaxy list", { portal.go(GalaxyFeedRoute()) }, Accent)
            }
        }

//        card {
//            row {
//                textBlock("Share and grow your talents.", modify(Flex1))
//                button("add talent", { portal.go(EditTalentRoute()) }, modify(Accent))
//            }
//        }

//        itemsBlock(userCache.talent.flow) { talent ->
//            navigation(TalentProfileRoute(talent.talentId)) {
//                card {
//                    textBlock(talent.name)
//                }
//            }
//        }
    }
}