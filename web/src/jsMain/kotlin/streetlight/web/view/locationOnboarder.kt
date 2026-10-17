package streetlight.web.view

import kampfire.model.storeOf
import koala.LottieFile
import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.*
import koala.modifier.*
import kotlinx.css.dvh
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.layout.BodyStyle
import streetlight.web.model.HostedLocations
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.NullLocation
import streetlight.web.model.getLocationEditor
import streetlight.web.model.getLocationOnboarder

context(star: Star)
fun ViewScope.locationOnboarder() {
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationOnboarder(editor, contentScope)

    context(model, editor) {
        flowBlock(model.focusState, modify(Magic, MinHeight(60.dvh))) { focus ->
            when (focus) {
                null -> column(modify(AlignItemsCenter, JustifyContentCenter)) {
                    centeredText("Looking for hosted locations...", Italic)
                    icon(SvgFile.Book.animated, Height(10))
                }
                is HostedLocations -> hostedLocations(focus.locations)
                NullLocation -> locationSearch()
                is LocationEdit -> column(BodyStyle.MainColumn) {
                    locationEditFormBody()
                    locationHostForm()
                    formSubmit("Next", model::submitLocation, editor.messages, Primary)
                }
                is LocationConfigContent -> when (focus.config.design) {
                    null -> locationConfigOnboarder()
                    else -> textBlock("Hello designed location!")
                }
                else -> error("unknown onboarder focus: $focus")
            }
        }
    }
}

/** The [hosted] locations of the star, to pick up where they left off, or to add a new one. */
context(model: LocationOnboarder)
private fun ViewScope.hostedLocations(hosted: List<LocationConfigContent>) = formRow {
    val hostedState = storeOf(hosted)
    formSection("Your Locations") {
        selectorContainer {
            selectorBlock(items = hostedState, selection = model.hostedSelectionState) { content ->
                searchItem(content.location.name, content.location.address, content.location.city)
            }
            button("add a new location", model::addNewLocation, Zen)
        }
    }
}

/** The name and address of the owner's location, and the locations found for them. */
context(model: LocationOnboarder)
private fun ViewScope.locationSearch() = formRow {
    formSection("Your Location") {
        textField(model.nameState, "name")
        textField(model.addressState, "address")
        messageBox(model.searchMessage)
        selectorContainer {
            selectorBlock(items = model.resultsState, selection = model.selectionState) { location ->
                searchItem(location.name, location.address, location.city)
            }
            flowBlock(model.canSearchOSMState) { canSearchOSM ->
                if (canSearchOSM) {
                    button("Not listed? Search OpenStreetMap", model::searchOSM, Zen)
                }
            }
        }
    }
}
