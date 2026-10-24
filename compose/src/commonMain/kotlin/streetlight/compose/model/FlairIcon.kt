package streetlight.compose.model

import koala.SvgFile
import koala.SvgPack

/** The icon badge of an entity type. The cousin of the web's `FlairIcon`. */
enum class FlairIcon(val svg: SvgPack) {
    Default(SvgFile.Flame),
    Event(SvgFile.Calendar),
    Location(SvgFile.MapPin),
    Media(SvgFile.Flame),
}
