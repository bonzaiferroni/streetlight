package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import koala.html.spacer
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.GalaxyConfigRoute

fun AppScope.viewGalaxyConfig(edit: GalaxyEdit) {
    val model = app.getGalaxyEditor(edit, parentScope)

    column {
        introSection("Galaxy Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your galaxy.")
        }

        tabs {
            tab("settings") {
                formBodyProto {
                    galaxyImageForm(model)
                    galaxyDescriptionForm(model)
                }
            }
            tab("map") {
                formBodyProto {
                    // galaxyCityForm(model)
                    galaxyLocationForm(model)
                }
            }
            tab("access") {
                formBodyProto {
                    galaxyAccessForm(model)
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

fun AppScope.viewGalaxyConfigRoute() {
    routeBlock<GalaxyConfigRoute, GalaxyEdit>(portal, { route ->
        api.readGalaxy(route.slug).handleResponse(toaster::toast)?.toEdit()
    }) {
        viewGalaxyConfig(it)
    }
}