package streetlight.compose.model

import koala.SvgFile
import koala.SvgPack

/** The icon badge of an entity type. */
enum class FlairIcon(val svg: SvgPack) {
    Default(SvgFile.Flame),
    Event(SvgFile.Calendar),
    Location(SvgFile.MapPin),
    Media(SvgFile.Flame),
}
