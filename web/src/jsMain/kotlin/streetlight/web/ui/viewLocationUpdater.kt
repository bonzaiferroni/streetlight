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
                locationUpdaterGreeting(star, model.stateNow.edit.name)
                locationEditFormBody(model)
                formSubmit(
                    label = "Next",
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
            viewHistory(content)
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

fun AppScope.locationUpdaterGreeting(star: Star, locationName: String?) = grid(
    IntroStyle.Columns, modify(AlignItemsCenter)
) {
    section(modify(IntroStyle.SectionMod)) {
        textBlock {
            +"Hello "
            em(star.username.value)
            +". You are a "
            strong("level ${star.scoutLevel} scout. ")
            if (star.scoutLevel in 0..1) {
                +"Your edits will become visible to the rest of Streetlight after they are reviewed. "
                +"Please take a moment to become familiar with the "
                navigation { +"content policy" }
                +" if you haven't already. "
            }
        }
        textBlock("Thank you for contributing, what can you tell us about ${locationName ?: "this location"}?")
    }
    lottie(LottieFile.StreetlightNight, modify(BorderRadius2, OverflowClip, MoonShadow))
}

private fun AppScope.viewHistory(content: LocationUpdaterContent) {
    val display = storeOf(TextDeltaDisplay.Combined)
    val dialog = dialog()

    launchEffect {
        display.flow.collect {
            dialog.element.setAttribute(TextDeltaStyle.Display.to(it))
        }
    }

    grid(columnsOf(1.fr, LinearDimension.auto)) {
        var previousEdit: LocationEdit? = null
        content.editLogs.forEachIndexed { index, log ->
            val timeDescription = log.createdAt.toRelativeTimeFormat()
            val edit = log.recordEdit as? LocationEdit
            val compareEdit = previousEdit
            textBlock("${log.username} ${log.editType.verb} the location $timeDescription")

            row(modify(JustifyContentEnd)) {
                if (log.editType == EditType.Update && index == content.editLogs.size - 1) {
                    button {
                        +"revert"
                    }
                }
                edit?.let {
                    button(onClick = {
                        dialog.updateContent(timeDescription, true) {
                            dialogCard {
                                row {
                                    spacer(modify(Flex1))
                                    dropMenu(display)
                                }
                                box {
                                    deltaGrid(mod = modify(TextDeltaStyle.Highlighter)) {
                                        deltaRow("name", edit.name, compareEdit?.name)
                                        deltaRow("address", edit.address, compareEdit?.address)
                                        deltaRow("description", edit.description, compareEdit?.description)
                                        deltaRow("geolocation", edit.geoPoint, compareEdit?.geoPoint)
                                    }
                                }
                            }
                        }
                    }) {
                        +"view"
                    }
                }
            }
            previousEdit = edit
        }
    }
}