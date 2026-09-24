package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.heading1
import koala.html.spacer
import streetlight.model.data.GalaxyConfig
import streetlight.model.data.toEdit
import streetlight.model.ui.GalaxyConfigRoute
import streetlight.web.layouts.route

fun ViewScope.viewGalaxyConfig(config: GalaxyConfig) {
    val model = app.getGalaxyEditor(config.galaxy.toEdit(config.marks), contentScope)
    val initialGalaxy = config.galaxy

    configBody("Galaxy", "Config", "viewGalaxyConfig.kt") {
        navigation(initialGalaxy.route) {
            heading1(initialGalaxy.name, TextAlignCenter)
        }

        lazyTabs {
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
            spacer(Flex1)

            messageBox(model.editMessage)
            button("Save", model::submit, Accent)
        }
    }
}

fun RouteScope.viewGalaxyConfigRoute() {
    routeBlock<GalaxyConfigRoute, GalaxyConfig> {
        viewGalaxyConfig(it)
    }
}