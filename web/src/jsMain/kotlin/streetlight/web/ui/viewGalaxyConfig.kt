package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.toEdit
import streetlight.web.GalaxyConfigRoute

fun AppScope.viewGalaxyConfig(edit: GalaxyEdit) {
    val model = app.getGalaxyEditor(edit, parentScope)

    column {
        introSection("Galaxy Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your galaxy.")
        }

        tabs {
            tab("settings") {
                formBody {
                    galaxyImageForm(model)
                    galaxyDescriptionForm(model)
                }
            }
            tab("map") {
                formBody {
                    // galaxyCityForm(model)
                    galaxyLocationForm(model)
                }
            }
            tab("access") {
                formBody {
                    galaxyAccessForm(model)
                }
            }
        }

        row(modify(JustifyContentSpaceBetween)) {
            button("back", onClick = { portal.goBack() })
            button("Save", modify(Accent), onClick = model::submit)
        }

        appFooter("")
    }
}

fun AppScope.viewGalaxyConfigRoute() {
    routeBlock<GalaxyConfigRoute, GalaxyEdit>(portal, { route ->
        api.readGalaxy(route.slug).handleOutcome(toaster::toast)?.toEdit()
    }) {
        viewGalaxyConfig(it)
    }
}