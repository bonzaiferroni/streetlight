package streetlight.web.layouts

import koala.Svg
import koala.SvgFile
import streetlight.model.data.PostType

/** The icon badge of an entity type. */
enum class FlairIcon(val small: Svg) {
    Default(SvgFile.FlameLarge),
    Event(SvgFile.Calendar.large),
    Location(SvgFile.MapPin.large),
    Media(SvgFile.FlameLarge),
}

val PostType.flair get() = when (this) {
    PostType.Event -> FlairIcon.Event
    PostType.Location -> FlairIcon.Location
    PostType.Media -> FlairIcon.Media
}