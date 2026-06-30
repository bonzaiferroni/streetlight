package streetlight.web.ui

import kabinet.utils.toRelativeTimeFormat
import kampfire.model.handleResponse
import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.BorderRadius2
import koala.css.Flex1
import koala.css.JustifyContentEnd
import koala.css.MoonShadow
import koala.css.OverflowClip
import koala.css.columnsOf
import koala.css.modify
import koala.dom.*
import koala.html.em
import koala.html.spacer
import koala.html.strong
import koala.model.storeOf
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import streetlight.model.data.EditType
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.data.verb
import streetlight.model.utils.TextDeltaDisplay
import streetlight.web.LocationRoute
import streetlight.web.UpdateLocationRoute

fun AppScope.viewLocationUpdater(content: LocationUpdaterContent, star: Star) {
    val edit = content.location.toEdit()
    val model = edit.let { app.getLocationEditor(it, parentScope) }

    tabs {
        tab("edit") {
            column {
                updaterGreeting(star, model.stateNow.edit.name ?: "this location")
                locationEditFormBody(model)
                formSubmit(
                    label = "Save",
                    onSubmit = {
                        launchEffect {
                            val location = model.submitSuspend()
                            if (location != null) {
                                portal.go(LocationRoute(location.slug))
                            }
                        }
                    },
                    messages = model.message,
                    back = LabeledAction("go back", portal::goBack)
                )
            }
        }
        tab("history") {
            viewEditHistory<LocationEdit>(content.editLogs) { edit, compareEdit ->
                deltaRow("name", edit.name, compareEdit?.name)
                deltaRow("address", edit.address, compareEdit?.address)
                deltaRow("description", edit.description, compareEdit?.description)
                deltaRow("geolocation", edit.geoPoint, compareEdit?.geoPoint)
            }
        }
    }
}

fun AppScope.viewUpdateLocationRoute() {
    column {
        starGate { star ->
            routeBlock<UpdateLocationRoute, LocationUpdaterContent?>(
                portal = portal,
                provideData = { route ->
                    api.readLocationUpdaterContent(route.slug).handleResponse(toaster::toast)
                }
            ) { content ->
                if (content == null) {
                    textBlock("something went wrong")
                    return@routeBlock
                }

                viewLocationUpdater(content, star)
            }
        }
        appFooter("")
    }
}