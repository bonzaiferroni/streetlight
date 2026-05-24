package streetlight.web

import kampfire.api.Slug

data class EventRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Event
    override val title get() = "Event"
}

data class EventUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.UpdateEvent
    override val title get() = "Post Event"
}

data class EventScoutRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.EventScout
    override val title get() = "Event Scout"
}