package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.spacer
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyConfig
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.GalaxyConfigRoute

fun ViewScope.viewGalaxyConfig(config: GalaxyConfig) {
    val model = app.getGalaxyEditor(config.galaxy.toEdit(config.marks), contentScope)

    column {
        introSection("Galaxy Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your galaxy.")
        }

        tabs {
            tab("settings") {
                formColumn {
                    galaxyDescriptionFormRow(model)
                }
            }
            tab("map") {
                formColumn {
                    galaxyMapFormRow(model)
                }
            }
            tab("access") {
                formColumn {
                    galaxyAccessFormRow(model)
                }
            }
            tab("Theme") {
                themeForm(model.designer.theme)
            }
            tab("Layout") {
                layoutBuilder(model.designer.layout)
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
    routeBlock<GalaxyConfigRoute, GalaxyConfig> {
        viewGalaxyConfig(it)
    }
}