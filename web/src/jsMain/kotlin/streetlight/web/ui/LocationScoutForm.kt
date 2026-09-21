package streetlight.web.ui

import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.html.spacer
import streetlight.web.layouts.ThemeColor
import streetlight.web.model.LocationScout

fun ViewScope.locationFinder(model: LocationScout) = formCard("Location Finder") {
    setStyle(Css.ColorScheme.of(ThemeColor.Location.cssValue))

    formRow {
        formSection("search") {
            row {
                textField(model.queryField, "search", modify(Flex1))
                textField(model.cityField, "city", modify(Width(24)))
            }
            centeredText("Don't see the location in the list? Try a power search with OpenStreetMap.", modify(MarginTop(1)))
            formSubmit("Search OSM", model::queryOSM, model.queryMessage, modify(Primary))
        }
        formSection("locations") {
            selectionBlock(
                items = model.locationsState, selection = model.selectionState,
                modifiers = modify(modify(Height(32), OverflowYAuto, ZenBg, Outline, BorderRadius1, Padding(1), TextSmall)),
                emptyText = "Start typing in the search box to see locations",
            ) { location ->
                searchItem(location.name, location.address, location.city)
            }
        }
    }

    formSection("or just put it on the map") {
        geoMapMount(mod = FormMod.GeoMap)
        row(modify(AlignItemsStart)) {
            button("What is Here?", model::whatIsHere)
            spacer(modify(Flex1))
            messageBox(model.mapMessage)
            textField(model.queryField)
            button("Create", onClick = model::createLocation, modify(Accent))
        }
    }
}

private fun ViewScope.searchItem(
    name: String?,
    address: String?,
    city: String?,
) = row(modify(AlignItemsCenter)) {
    icon(SvgFile.MapPinLarge, modify(Height(5), ColorSchemeFg))
    column(modify(Flex1, Gap0)) {
        textBlock(name ?: "a location", modify(Bold))
        spacer(modify(Height2Px, InkGradientBg))
        address?.let {
            textBlock(it)
        }
    }
    city?.let {
        icon(SvgFile.City, modify(Height(3), ColorSchemeFg))
        textBlock(it, modify(OpacityHigh, Width(16)))
    }
}