package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.btn
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.data.Star
import streetlight.model.ui.StarConfigRoute
import streetlight.model.ui.EditTalentRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.GalaxyListRoute
import streetlight.model.ui.SandboxRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.TalentProfileRoute
import streetlight.web.model.DataCache
import streetlight.web.model.StarSession
import streetlight.model.ui.toRoute

fun ViewScope.viewStarDash(star: Star) {
    column(mod = BodyStyle.Mod) {
        tabs {
            tab("activity") {
                activityContent(star)
            }
            tab("sandbox") {
                sandboxContent(star)
            }
        }
    }
}

fun ViewScope.viewStarDashRoute() {
    routeBlock<StarDashRoute>() {
        starGate { star ->
            viewStarDash(star)
        }
    }
}

private fun ViewScope.activityContent(star: Star) {
    grid {
        section("galaxies") {
            request(api::readUserGalaxies) { galaxies ->
                galaxies.forEach { galaxy ->
                    grid(columnsOf(1.fr, LinearDimension.auto)) {
                        navigation(galaxy.toRoute()) {
                            listingOf(galaxy.name, galaxy.image?.thumb)
                        }
                        starToggle(galaxy.isLit, galaxy.galaxyId)
                    }
                }
            }
        }
        section("edits") {
            request(api::readPendingEdits) { logs ->
                logs.forEach { log ->
                    val label = log.recordEdit?.label ?: return@forEach
                    listingOf(label, log.recordEdit?.image?.thumb)
                }
            }
        }
        val dialog = dialog()
        section("requests") {
            request(api::readUserTasks) { tasks ->
                tasks.forEach { task ->
                    textBlock(task.label).onClick {
                        dialog.updateContent(task.label, true) {
                            dialogCard {
                                taskContent(task)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ViewScope.sandboxContent(star: Star) {
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
                btn("Edit Profile", StarConfigRoute)
                btn("Go to sandbox", SandboxRoute)
                button("galaxy list", { portal.go(GalaxyListRoute) }, modify(Accent))
            }
        }

        card {
            row {
                textBlock("Share and grow your talents.", modify(Flex1))
                button("add talent", { portal.go(EditTalentRoute()) }, modify(Accent))
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