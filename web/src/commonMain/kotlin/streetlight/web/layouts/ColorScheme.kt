package streetlight.web.layouts

import streetlight.model.data.PostType

enum class ColorScheme(val cssValue: String) {
    Accent("var(--accent-fg)"),
    Primary("var(--primary-fg)"),
    Galaxy("var(--sea-green-fg)"),
    Event("var(--accent-fg)"),
    Location("var(--green-fg)"),
    City("var(--yellow-fg)"),
    Media("var(--primary-fg)"),
}

val PostType.colorScheme get() = when (this) {
    PostType.Event -> ColorScheme.Event
    PostType.Location -> ColorScheme.Location
    PostType.Media -> ColorScheme.Media
}