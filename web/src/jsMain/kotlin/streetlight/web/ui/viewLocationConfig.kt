package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.topLogo
import koala.model.storeOf
import streetlight.model.data.DefaultLayout
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.LocationContent
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationConfigRoute
import streetlight.web.model.LayoutEditor
import streetlight.web.pages.formSubmit
import streetlight.web.shells.cardOf

fun ViewScope.viewLocationConfig(
    content: LocationConfigContent,
) {
    val location = content.location
    val configField = storeOf(content.config)
    val layoutEditor = LayoutEditor(configField.now.layout ?: DefaultLayout.location)
    column(BodyStyle.column) {
        topLogo()
        cardOf(location)
        tabs(Id("location-config-tabs")) {
            tab("profile") {
                val edit = location.toEdit()
                // viewLocationEditor(edit, app, null, false, null)
            }
            tab("layout") {
                dataBlock({ api.readLocationEvents(location.slug) }) { events ->
                    // td: fix layout source
                    val locationContent = LocationContent(
                        location = location,
                        layout = configField.now.layout ?: DefaultLayout.location,
                        events = events,
                        canEdit = true
                    )

                    column {
                        val messages = MessageStore()
                        layoutBuilder(layoutEditor, locationContent)
                        formSubmit("save layout", {
                            val layout = layoutEditor.buildLayout() ?: return@formSubmit
                            configField.set { copy(layout = layout) }
                            launchEffect("upload layout") {
                                api.updateLocationConfig(configField.now).handleResponse(messages)
                            }
                        }, messages)
                    }
                }
            }
            tab("events") {
                column {
                    location.eventsUrl.let { link ->
                        row {
                            textBlock("This location has an event page that we can try to read.", modify(Flex1))
                            button("read events", onClick = {
                                // app.portal.go(OldEventScoutRoute(location, link))
                            })
                        }
                    }
                    dataBlock({ api.readLocationEvents(location.slug) }) { events ->
                        column {
                            events.forEach { event ->
                                cardOf(event)
                            }
                        }
                    }
                }
            }
            tab("automate") {
                locationAutomationForm(content)
            }
        }
        appFooter("")
    }
}

fun RouteScope.viewLocationConfigRoute() {
    routeBlock<LocationConfigRoute, LocationConfigContent> { config ->
        viewLocationConfig(config)
    }
}