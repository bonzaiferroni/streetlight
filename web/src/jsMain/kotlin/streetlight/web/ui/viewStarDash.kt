package streetlight.web.ui

import kampfire.model.thumb
import koala.css.*
import koala.dom.*
import koala.html.btn
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.data.Star
import streetlight.web.EditStarRoute
import streetlight.web.EditTalentRoute
import streetlight.web.HomeRoute
import streetlight.web.GalaxyListRoute
import streetlight.web.GalaxyRoute
import streetlight.web.SandboxRoute
import streetlight.web.StarDashRoute
import streetlight.web.TalentProfileRoute
import streetlight.web.model.DataCache
import streetlight.web.model.StarSession
import streetlight.web.toRoute

fun AppScope.viewStarDash(star: Star) {
    tabs {
        tab("activity") {
            activityContent(star)
        }
        tab("sandbox") {
            sandboxContent(star)
        }
    }
}

fun AppScope.viewStarDashRoute() {
    routeBlock<StarDashRoute>() {
        starGate { star ->
            viewStarDash(star)
        }
    }
}

private fun AppScope.activityContent(star: Star) {
    column {
        section("galaxies") {
            request({ api.readGalaxies() }) { galaxies ->
                galaxies.forEach { galaxy ->
                    grid(columnsOf(1.fr, LinearDimension.auto)) {
                        navigation(galaxy.toRoute()) {
                            listingOf(galaxy.name, galaxy.images.thumb)
                        }
                        starToggle(galaxy.isLit, galaxy.galaxyId)
                    }
                }
            }
        }
    }
}

private fun AppScope.sandboxContent(star: Star) {
    // val model = UserHub(renderScope, app.client.api)
    val userCache = app.get<DataCache>()
    val gate = app.get<StarSession>()

    column {
        row {
            textBlock("Hello ${star.username}!", modify(Flex1))
            button("go home", onClick = { portal.go(HomeRoute) })
            button("sign out", onClick = gate::signOut)
        }

        card {
            row {
                textBlock("Add things to the map.", modify(Flex1))
                btn("Edit Profile", EditStarRoute)
                btn("Go to sandbox", SandboxRoute)
                button("galaxy list", modify(Accent), onClick = { portal.go(GalaxyListRoute) })
            }
        }

        card {
            row {
                textBlock("Share and grow your talents.", modify(Flex1))
                button("add talent", modify(Accent), onClick = { portal.go(EditTalentRoute()) })
            }
        }

        itemsBlock(userCache.talent.flow) { talent ->
            navigation(TalentProfileRoute(talent.talentId)) {
                card {
                    textBlock(talent.name)
                }
            }
        }
    }
}