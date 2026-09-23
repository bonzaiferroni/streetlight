package streetlight.web.ui

import koala.dom.*
import koala.dom.MenuAction
import streetlight.model.data.City
import streetlight.model.data.toEdit
import streetlight.model.ui.CityConfigRoute
import streetlight.web.model.CityEditor

fun ViewScope.viewCityConfig(city: City) {
    val model = CityEditor(city.toEdit(), contentScope, api, portal)
    val messenger = MessageStore()

    column(BodyStyle.MainColumn) {
        formColumn {
            cityDetailsForm(model)
            cityImageForm(model)
            formSubmit(
                label = "Save",
                onClick = { model.submit(messenger) },
                messenger = messenger,
                back = MenuAction("go back", onClick = portal::goBack),
            )
        }
        appFooter(sourcePathUi("viewCityConfig.kt"))
    }
}

fun RouteScope.viewCityConfigRoute() {
    routeBlock<CityConfigRoute, City> { city ->
        starGate {
            viewCityConfig(city)
        }
    }
}
