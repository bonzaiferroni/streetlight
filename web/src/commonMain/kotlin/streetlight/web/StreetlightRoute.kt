package streetlight.web

import koala.html.AppRoute

enum class AppScreen(val path: String) {
    Home("home"),
    Account("account"),
    Event("event")
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