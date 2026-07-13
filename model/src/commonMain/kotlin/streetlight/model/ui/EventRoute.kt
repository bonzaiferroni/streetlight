package streetlight.model.ui

import kampfire.api.Slug

data class EventRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.Event
    override val title get() = "Event"
}

data class EventUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.UpdateEvent
    override val title get() = "Post Event"
}

data class EventScoutRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = Screen.EventScout
    override val title get() = "Event Scout"
}