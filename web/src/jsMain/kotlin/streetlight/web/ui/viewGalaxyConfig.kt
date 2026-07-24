package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.spacer
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.GalaxyConfigRoute

fun ViewScope.viewGalaxyConfig(edit: GalaxyEdit) {
    val model = app.getGalaxyEditor(edit, contentScope)

    column {
        introSection("Galaxy Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your galaxy.")
        }

        tabs {
            tab("settings") {
                form {
                    galaxyDescriptionFormRow(model)
                }
            }
            tab("map") {
                form {
                    galaxyMapFormRow(model)
                }
            }
            tab("access") {
                form {
                    galaxyAccessFormRow(model)
                }
            }
        }

        row {
            button("back", { portal.goBack() })
            spacer(modify(Flex1))

            messageBox(model.editMessage)
            button("Save", model::submit, modify(Accent))
        }

        appFooter("")
    }
}

fun RouteScope.viewGalaxyConfigRoute() {
    routeBlock<GalaxyConfigRoute, Galaxy> {
        viewGalaxyConfig(it.toEdit())
    }
}