package streetlight.web.layouts

import koala.Svg
import koala.SvgFile
import streetlight.model.data.PostType

enum class FlairIcon(val small: Svg) {
    Default(SvgFile.FlameLarge),
    Event(SvgFile.CalendarLarge),
    Location(SvgFile.MapPinLarge),
    Media(SvgFile.FlameLarge),
}

val PostType.flair get() = when (this) {
    PostType.Event -> FlairIcon.Event
    PostType.Location -> FlairIcon.Location
    PostType.Media -> FlairIcon.Media
}