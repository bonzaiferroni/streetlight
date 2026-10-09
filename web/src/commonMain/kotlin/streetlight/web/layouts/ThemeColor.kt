package streetlight.web.layouts

import koala.modifier.Koala
import koala.modifier.Rgb
import koala.modifier.mix
import streetlight.model.data.PostType

/** The color of an entity type, as a CSS value and as the [rgb] it is mixed from. */
enum class ThemeColor(val cssValue: String, val rgb: Rgb) {
    Accent("var(--accent-fg)", Koala.accent),
    Primary("var(--primary-fg)", Koala.primary),
    Galaxy("var(--sea-green-fg)", Koala.seaGreen),
    Event("var(--accent-fg)", Koala.accent),
    Location("var(--green-fg)", Koala.green),
    City("var(--yellow-fg)", Koala.yellow),
    Media("var(--primary-fg)", Koala.primary),
}

/** The color of a marker's light: near white, with a hint of the theme color. */
val ThemeColor.light get() = rgb.mix(Rgb.White, .8f)

val PostType.themeColor get() = when (this) {
    PostType.Event -> ThemeColor.Event
    PostType.Location -> ThemeColor.Location
    PostType.Media -> ThemeColor.Media
}