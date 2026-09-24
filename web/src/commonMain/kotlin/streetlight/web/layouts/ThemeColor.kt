package streetlight.web.layouts

import streetlight.model.data.PostType

/** The color of an entity type, as a CSS value. */
enum class ThemeColor(val cssValue: String) {
    Accent("var(--accent-fg)"),
    Primary("var(--primary-fg)"),
    Galaxy("var(--sea-green-fg)"),
    Event("var(--accent-fg)"),
    Location("var(--green-fg)"),
    City("var(--yellow-fg)"),
    Media("var(--primary-fg)"),
}

val PostType.themeColor get() = when (this) {
    PostType.Event -> ThemeColor.Event
    PostType.Location -> ThemeColor.Location
    PostType.Media -> ThemeColor.Media
}