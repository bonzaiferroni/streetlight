package streetlight.web

import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.Event
import streetlight.model.data.EventId

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> StreetlightRoute?
): AppScreen {
    Home("home", { HomeRoute() }),
    Account("account", { AccountRoute }),
    Event("event", { segments -> segments.getOrNull(1)?.let { EventIdRoute(EventId(it)) } }),
    CreateEvent("create-event", { CreateEventRoute }),
    Sandbox("sandbox", { SandboxRoute }),
    FullMap("full-map", { FullMapRoute })
}

sealed interface StreetlightRoute: AppRoute

data class HomeRoute(
    val tab: String? = null
): StreetlightRoute {
    override val screen get() = StreetlightScreen.Home
}

object AccountRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Account
}

sealed interface EventRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Event
}

data class EventIdRoute(
    val eventId: EventId
): EventRoute {
    override fun toHashPath() = "${super.toHashPath()}/${eventId.value}"
}

data class EventObjectRoute(val event: Event): EventRoute {
    override fun toHashPath() = "${super.toHashPath()}/${event.eventId.value}"
}

object CreateEventRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.CreateEvent
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Sandbox
}

object FullMapRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.FullMap
}