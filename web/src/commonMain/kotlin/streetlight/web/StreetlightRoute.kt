package streetlight.web

import kampfire.api.TableId
import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.PostId
import streetlight.model.data.SongId

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> AppRoute?
): AppScreen {
    Home("home", { HomeRoute() }),
    Account("account", { AccountRoute }),
    Event("event", { path -> path.provideRouteFromPath { EventIdRoute(EventId(it)) }  }),
    EditEvent("edit-event", { path -> EditEventRoute(path.provideId { EventId(it) }) }),
    EditStory("edit-story", { path -> EditPostRoute(path.provideId { PostId(it) }) }),
    Sandbox("sandbox", { SandboxRoute }),
    FullMap("full-map", { FullMapRoute }),
    Chat("chat", { ChatRoute }),
    SongProfile("song-profile", { path -> path.provideRouteFromPath { SongProfileRoute(SongId(it)) } }),
}

fun List<String>.provideRouteFromPath(argIndex: Int = 1, provideRoute: (String) -> AppRoute?) =
    getOrNull(argIndex)?.let { provideRoute(it) }

fun <T: TableId<String>> List<String>.provideId(argIndex: Int = 1, provideId: (String) -> T) =
    getOrNull(argIndex)?.let { provideId(it) }

sealed interface StreetlightRoute: AppRoute

sealed interface StringIdRoute: StreetlightRoute {
    val id: TableId<String>?

    override fun toHashPath() = id?.let { "${super.toHashPath()}/${it.value}" } ?: super.toHashPath()
}

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
    override val id: EventId
): EventRoute, StringIdRoute

data class EventObjectRoute(val event: Event): EventRoute, StringIdRoute {
    override val id get() = event.eventId
}

data class EditEventRoute(val eventId: EventId? = null): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.EditEvent
    override val id get() = eventId
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Sandbox
}

object FullMapRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.FullMap
}

data class EditPostRoute(
    val postId: PostId? = null
): StreetlightRoute {
    override val screen get() = StreetlightScreen.EditStory
}

object ChatRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Chat
}

data class SongProfileRoute(
    val songId: SongId
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.SongProfile
    override val id get() = songId
}