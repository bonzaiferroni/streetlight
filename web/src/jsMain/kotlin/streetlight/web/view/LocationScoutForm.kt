package streetlight.web.view

import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import kampfire.model.tapOf
import koala.component.spacer
import streetlight.model.data.EntityType
import streetlight.web.model.LocationScout
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.FormMod
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.cssValue

context(model: LocationScout)
fun ViewScope.locationFinder() = formCard("Location Finder") {
    setStyle(Css.ColorScheme.of(EntityType.Location.cssValue))

    formRow {
        formSection("search") {
            row {
                textField(model.queryState, "search", Flex1)
                textField(model.cityState, "city", Width(24))
            }
            selectorContainer {
                selectorBlock(items = model.locationsState, selection = model.selectionState) { location ->
                    searchItem(location.name, location.address, location.city)
                }
                flowBlock(model.queryState.tapOf { it.isEmpty() }) { isQueryEmpty ->
                    when (isQueryEmpty) {
                        true -> centeredText("Start typing in the search box to see locations")
                        false -> column(AlignItemsCenter) {
                            centeredText("Don't see the location in the list? Try a power search with OpenStreetMap.")
                            formSubmit("Search OSM", model::queryOSM, model.queryMessage, Primary)
                        }
                    }
                }
            }
        }
    }

    formSection("or just put it on the map") {
        geoMapMount(model.map, mod = FormMod.GeoMap)
        row(AlignItemsStart) {
            button("What is Here?", model::whatIsHere)
            spacer(Flex1)
            messageBox(model.mapMessage)
            textField(model.queryState)
            button("Create", onClick = model::createLocation, Accent)
        }
    }
}

/** A row naming a location found in a search, with its [address] and [city] when known. */
fun ViewScope.searchItem(
    name: String?,
    address: String?,
    city: String?,
) = row(AlignItemsCenter) {
    icon(SvgFile.MapPin.large, modify(Height(5), ColorSchemeFg))
    column(modify(Flex1, Gap0)) {
        textBlock(name ?: "a location", Bold)
        spacer(modify(Height2Px, InkGradientBg))
        address?.let {
            textBlock(it)
        }
    }
    city?.let {
        icon(SvgFile.City.small, modify(Height(3), ColorSchemeFg))
        textBlock(it, modify(OpacityHigh, Width(16)))
    }
}