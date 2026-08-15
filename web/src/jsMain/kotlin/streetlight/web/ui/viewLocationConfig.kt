package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.heading1
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
        column(modify(Gap0, MarginTop1)) {
            filigree {
                heading3("configure", modify(TextTransformUppercase, OpacityHalf))
            }
            heading1(location.name, modify(TextAlignCenter))
        }

        tabs(Id("location-config-tabs")) {
            tab("profile") {
                val edit = location.toEdit()
                // viewLocationEditor(edit, app, null, false, null)
            }
            tab("theme") {
                themeForm(themeEditor)
            }
            tab("layout") {
                dataBlock({ api.readLocationEvents(location.slug) }) { events ->
                    // td: fix layout source
                    val locationContent = LocationContent(
                        location = location,
                        design = PageDesign(initialLayout, null),
                        events = events,
                        canEdit = true
                    )

                    layoutBuilder(layoutEditor, locationContent)
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
        val messages = MessageStore()
        formSubmit("save config", {
            val layout = layoutEditor.buildLayout()
            val theme = themeEditor.buildTheme()
            configState.set { copy(design = PageDesign(layout, theme)) }
            launchEffect("save config") {
                messages.deliverSending()
                api.updateLocationConfig(configState.now).handleResponse(messages, "Design saved.")
            }
        }, messages)
        appFooter("")
    }
}

fun RouteScope.viewLocationConfigRoute() {
    routeBlock<LocationConfigRoute, LocationConfigContent> { config ->
        viewLocationConfig(config)
    }
}