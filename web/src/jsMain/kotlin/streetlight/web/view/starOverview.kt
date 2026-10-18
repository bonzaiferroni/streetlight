package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.*
import koala.model.MenuAction
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.ui.LocationConfigRoute
import streetlight.model.ui.toRoute
import streetlight.web.dom.layout.dashEntity
import streetlight.web.dom.layout.dataBlock
import streetlight.web.dom.layout.dashSectionHeading
import streetlight.web.model.api
import streetlight.web.model.portal

fun ViewScope.starOverview() {
    column {
        section {
            dashSectionHeading("Locations", emptyList())
            dataBlock(api.location::readHostedLocations, Gap2Px) { locations ->
                locations.forEach { location ->
                    dashEntity(location, listOf(
                        MenuAction("Edit") { portal.go(LocationConfigRoute(location.locationId)) },
                    ))
                }
            }
        }
        section {
            dashSectionHeading("Galaxies", emptyList())
            dataBlock(api.galaxy::readUserGalaxies) { galaxies ->
                galaxies.forEach { galaxy ->
                    grid(columnsOf(1.fr, LinearDimension.auto)) {
                        navigation(galaxy.toRoute()) {
                            // listingOf(galaxy.name, galaxy.image?.thumb)
                        }
                        // starToggle(galaxy.isLit, galaxy.galaxyId)
                    }
                }
            }
        }

//        section("edits") {
//            sectionHeading("Galaxies", emptyList())
//            dataBlock(api.star::readPendingEdits) { logs ->
//                logs.forEach { log ->
//                    val label = log.recordEdit?.label ?: return@forEach
//                    // listingOf(label, log.recordEdit?.image?.thumb)
//                }
//            }
//        }
//        val dialog = dialog()
//        section("requests") {
//            dataBlock(api::readUserTasks) { tasks ->
//                tasks.forEach { task ->
//                    textBlock(task.label).onClick {
//                        dialog.updateContent(task.label, true) {
//                            dialogCard {
//                                taskContent(task)
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
}

