package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.heading3
import koala.html.topLogo
import koala.model.storeOf
import streetlight.model.data.DefaultLayout
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.LocationContent
import streetlight.model.data.PageDesign
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationConfigRoute
import streetlight.web.model.LayoutEditor
import streetlight.web.model.ThemeEditor
import streetlight.web.shells.cardOf

fun ViewScope.viewLocationConfig(
    content: LocationConfigContent,
) {
    val location = content.location
    val configState = storeOf(content.config)
    val initialLayout = configState.now.design?.layout ?: DefaultLayout.location
    val layoutEditor = LayoutEditor(initialLayout)
    val themeEditor = ThemeEditor(content.config.design?.theme)
    column(BodyStyle.column) {
        topLogo()
        cardOf(location)
        tabs(Id("location-config-tabs")) {
            tab("profile") {
                val edit = location.toEdit()
                // viewLocationEditor(edit, app, null, false, null)
            }
            tab("design") {
                dataBlock({ api.readLocationEvents(location.slug) }) { events ->
                    // td: fix layout source
                    val locationContent = LocationContent(
                        location = location,
                        design = PageDesign(initialLayout, null),
                        events = events,
                        canEdit = true
                    )

                    column {
                        val messages = MessageStore()
                        column(BodyStyle.column) {
                            column {
                                filigree { heading3("Theme") }
                                themeForm(themeEditor)
                            }

                            column {
                                filigree { heading3("Layout") }
                                layoutBuilder(layoutEditor, locationContent)
                            }

                        }

                        formSubmit("save design", {
                            val layout = layoutEditor.buildLayout()
                            val theme = themeEditor.buildTheme()
                            configState.set { copy(design = PageDesign(layout, theme)) }
                            launchEffect("save design") {
                                messages.deliverSending()
                                api.updateLocationConfig(configState.now).handleResponse(messages, "Design saved.")
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
            tab("settings") {
                locationSettingsForm(configState)
                // locationAutomationForm(content)
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