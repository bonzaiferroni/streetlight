package streetlight.web.view

import koala.dom.*
import koala.model.MenuAction
import streetlight.model.data.City
import streetlight.model.data.toEdit
import streetlight.model.ui.CityConfigRoute
import streetlight.model.ui.CityRoute
import streetlight.web.model.CityEditor
import koala.model.*
import streetlight.web.model.api
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.portal

fun ViewScope.viewCityConfig(city: City) {
    val model = CityEditor(city.toEdit(), contentScope, api, portal)
    val messenger = MessageStore()

    configBody("City", "Config", "viewCityConfig.kt") {
        configHeading(city.name, CityRoute(city.slug))

        context(model) {
            cityDetailsForm()
            cityLinksForm()
        }
        formSubmit(
            label = "Save",
            onClick = { model.submit(messenger) },
            messenger = messenger,
            back = MenuAction("go back", onClick = portal::goBack),
        )
    }
}

fun RouteScope.viewCityConfigRoute() {
    starRouteBlock<CityConfigRoute, City> { city ->
        viewCityConfig(city)
    }
}
