package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.dom.routeBlock
import koala.component.spacer
import streetlight.model.data.GalaxyConfig
import streetlight.model.data.toEdit
import streetlight.model.ui.GalaxyConfigRoute
import streetlight.web.model.route
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.formColumn
import streetlight.web.model.getGalaxyEditor
import streetlight.web.model.portal

fun ViewScope.viewGalaxyConfig(config: GalaxyConfig) {
    val model = app.getGalaxyEditor(config.galaxy.toEdit(config.marks), contentScope)
    val initialGalaxy = config.galaxy

    configBody("Galaxy", "Config", "viewGalaxyConfig.kt") {
        configHeading(initialGalaxy.name, initialGalaxy.route)

        context(model) {
            lazyTabs {
                tab("settings") {
                    formColumn {
                        galaxyDescriptionFormRow()
                    }
                }
                tab("map") {
                    formColumn {
                        galaxyMapFormRow()
                    }
                }
                tab("access") {
                    formColumn {
                        galaxyAccessFormRow()
                    }
                }
                tab("Theme") {
                    context(model.designer.theme) {
                        themeForm()
                    }
                }
                tab("Layout") {
                    context(model.designer.layout) {
                        layoutBuilder()
                    }
                }
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