package streetlight.web

import koala.html.AppRoute

enum class AppScreen(val path: String) {
    Home("home"),
    Account("account"),
    Event("event"),
    CreateLocation("create-location"),
    CreateEvent("create-event"),
}

sealed class StreetlightRoute: AppRoute {
    abstract val screen: AppScreen

    override fun toHashPath() = "#${screen.path}"

    companion object
}

data class Home(
    val tab: String? = null
): StreetlightRoute() {
    override val screen get() = AppScreen.Home

    companion object {
        val screen get() = AppScreen.Home
    }
}

object Account: StreetlightRoute() {
    override val screen get() = AppScreen.Account
}

object EventRoute: StreetlightRoute() {
    override val screen get() = AppScreen.Event
}

object CreateLocationRoute : StreetlightRoute() {
    override val screen get() = AppScreen.CreateLocation
}

object CreateEventRoute : StreetlightRoute() {
    override val screen get() = AppScreen.CreateEvent
}