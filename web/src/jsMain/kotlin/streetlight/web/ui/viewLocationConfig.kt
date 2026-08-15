package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.heading1
import koala.html.heading3
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
    val locationState = storeOf(content.location)
    val configState = storeOf(content.config)
    val initialLayout = configState.now.design?.layout ?: DefaultLayout.location
    val layoutEditor = LayoutEditor(initialLayout)
    val themeEditor = ThemeEditor(content.config.design?.theme)
    val saveMessages = MessageStore()

    fun saveConfig() {
        val layout = layoutEditor.buildLayout()
        val theme = themeEditor.buildTheme()
        configState.set { copy(design = PageDesign(layout, theme)) }
        launchEffect("save config") {
            saveMessages.deliverSending()
            api.updateLocationConfig(configState.now).handleResponse(saveMessages, "Config saved.")
        }
    }

    column(BodyStyle.column) {
        column(modify(Gap0, MarginTop1)) {
            filigree {
                heading3("configure", modify(TextTransformUppercase, OpacityHalf))
            }
            flowBlock(locationState) { location ->
                box {
                    heading1(location.name, modify(TextAlignCenter))
                }
            }
        }

        tabs(Id("location-config-tabs")) {
            tab("profile") {
                val editor = app.getLocationEditor(locationState.now.toEdit(), contentScope)
                column {
                    locationEditFormBody(editor)
                    formSubmit(
                        label = "Save profile",
                        onSubmit = {
                            launchEffect {
                                val editedLocation = editor.submitSuspend() ?: return@launchEffect
                                editor.messages.deliverSuccess("Profile saved")
                                locationState.set { editedLocation }
                            }
                        },
                        messenger = editor.messages,
                    )
                }
            }
            tab("theme") {
                column {
                    themeForm(themeEditor)
                    formSubmit("save config", ::saveConfig, saveMessages)
                }
            }
            tab("layout") {
                val location = locationState.now
                dataBlock({ api.readLocationEvents(location.slug) }) { events ->
                    // td: fix layout source
                    val locationContent = LocationContent(
                        location = location,
                        design = PageDesign(initialLayout, null),
                        events = events,
                        canEdit = true
                    )

                    column {
                        layoutBuilder(layoutEditor, locationContent)
                        formSubmit("save config", ::saveConfig, saveMessages)
                    }
                }
            }
            tab("events") {
                val location = locationState.now
                column {
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