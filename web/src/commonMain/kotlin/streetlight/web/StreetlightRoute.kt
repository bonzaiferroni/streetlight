package streetlight.web

import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.EventId

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> StreetlightRoute?
): AppScreen<StreetlightRoute> {
    Home("home", { HomeRoute() }),
    Account("account", { AccountRoute }),
    Event("event", { segments -> segments.getOrNull(1)?.let { EventRoute(EventId(it)) } }),
    CreateEvent("create-event", { CreateEventRoute }),
}

sealed class StreetlightRoute: AppRoute

data class HomeRoute(
    val tab: String? = null
): StreetlightRoute() {
    override val screen get() = StreetlightScreen.Home

    companion object {
        val screen get() = StreetlightScreen.Home
    }
}

object AccountRoute: StreetlightRoute() {
    override val screen get() = StreetlightScreen.Account
}

data class EventRoute(
    val id: EventId
): StreetlightRoute() {
    override val screen get() = StreetlightScreen.Event
    override fun toHashPath() = "${super.toHashPath()}/${id.value}"
}

object CreateEventRoute: StreetlightRoute() {
    override val screen get() = StreetlightScreen.CreateEvent
}