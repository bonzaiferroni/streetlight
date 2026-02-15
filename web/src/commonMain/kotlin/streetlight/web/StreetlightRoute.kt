package streetlight.web

import kampfire.api.TableId
import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.StoryId

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> AppRoute?
): AppScreen {
    Home("home", { HomeRoute() }),
    Account("account", { AccountRoute }),
    Event("event", { path -> path.provideRouteFromPath { EventIdRoute(EventId(it)) }  }),
    EditEvent("edit-event", { path -> EditEventRoute(path.provideId { EventId(it) }) }),
    EditStory("edit-story", { path -> EditStoryRoute(path.provideId { StoryId(it) }) }),
    Sandbox("sandbox", { SandboxRoute }),
    FullMap("full-map", { FullMapRoute }),
    Chat("chat", { ChatRoute })
}

fun List<String>.provideRouteFromPath(argIndex: Int = 1, provideRoute: (String) -> AppRoute?) =
    getOrNull(argIndex)?.let { provideRoute(it) }

fun <T: TableId<String>> List<String>.provideId(argIndex: Int = 1, provideId: (String) -> T) =
    getOrNull(argIndex)?.let { provideId(it) }

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

data class EditEventRoute(val eventId: EventId? = null): StreetlightRoute {
    override val screen get() = StreetlightScreen.EditEvent
    override fun toHashPath() = eventId?.let { "${super.toHashPath()}/${it.value}"} ?: super.toHashPath()
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Sandbox
}

object FullMapRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.FullMap
}

data class EditStoryRoute(
    val storyId: StoryId? = null
): StreetlightRoute {
    override val screen get() = StreetlightScreen.EditStory
}

object ChatRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Chat
}