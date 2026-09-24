package streetlight.web.ui

import kampfire.model.toDataOrNull
import koala.modifier.*
import koala.dom.*
import koala.html.Id
import koala.html.heading1
import kampfire.model.storeOf
import streetlight.model.data.DefaultLayout
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.PageDesign
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationConfigRoute
import streetlight.web.layouts.route
import streetlight.web.model.DesignEditor
import streetlight.web.shells.cardOf

fun ViewScope.viewLocationConfig(
    content: LocationConfigContent,
) {
    val locationState = storeOf(content.location)
    val configState = storeOf(content.config)
    val initialLayout = configState.now.design?.layout
    val designer = DesignEditor(api, DefaultLayout.location, PageDesign(initialLayout, content.config.design?.theme))
    val saveMessages = MessageStore()

    fun saveConfig() {
        launchEffect("save config") {
            val design = designer.build(saveMessages)
            configState.set { copy(design = design) }
            saveMessages.deliverSending()
            api.location.updateLocationConfig(configState.now).toDataOrNull(saveMessages, "Config saved.")
        }
    }

    configBody("Location", "Config", "viewLocationConfig.kt") {
        flowBlock(locationState) { location ->
            navigation(location.route) {
                heading1(location.name, TextAlignCenter)
            }
        }

        lazyTabs(Id("location-config-tabs")) {
            tab("profile") {
                val editor = app.getLocationEditor(locationState.now.toEdit(), contentScope)
                column {
                    locationEditFormBody(editor)
                    formSubmit(
                        label = "Save profile",
                        onClick = {
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
                    themeForm(designer.theme)
                    formSubmit("save config", ::saveConfig, saveMessages)
                }
            }
            tab("layout") {
                column {
                    layoutBuilder(designer.layout)
                    formSubmit("save config", ::saveConfig, saveMessages)
                }
            }
            tab("events") {
                val location = locationState.now
                column {
                    dataBlock({ api.event.readLocationEvents(location.slug) }) { events ->
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
    }
}

fun RouteScope.viewLocationConfigRoute() {
    routeBlock<LocationConfigRoute, LocationConfigContent> { config ->
        viewLocationConfig(config)
    }
}