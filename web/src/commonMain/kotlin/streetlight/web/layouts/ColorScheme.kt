package streetlight.web.layouts

enum class ColorScheme(val cssValue: String) {
    Accent("var(--accent-fg)"),
    Primary("var(--primary-fg)"),
    Galaxy("var(--sea-green-fg)"),
    Event("var(--accent-fg)"),
    Location("var(--green-fg)"),
    City("var(--yellow-fg)"),
    Media("var(--primary-fg)"),
}