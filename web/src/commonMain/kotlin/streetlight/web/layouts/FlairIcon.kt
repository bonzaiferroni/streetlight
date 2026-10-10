package streetlight.web.layouts

import koala.SvgPack
import koala.SvgFile
import streetlight.model.data.PostType

/** The icon badge of an entity type. */
enum class FlairIcon(val svg: SvgPack) {
    Default(SvgFile.Flame),
    Event(SvgFile.Calendar),
    Location(SvgFile.MapPin),
    Media(SvgFile.Flame),
}

val PostType.flair get() = when (this) {
    PostType.Event -> FlairIcon.Event
    PostType.Location -> FlairIcon.Location
    PostType.Media -> FlairIcon.Media
}