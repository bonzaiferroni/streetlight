package koala.modifier

import kotlinx.css.Align
import kotlinx.css.Flex
import kotlinx.css.JustifyContent
import kotlinx.css.px

val Height get() = Css.Height
val Padding get() = Css.Padding
val Margin get() = Css.Margin
val Gap get() = Css.Gap
val MaxWidth get() = Css.MaxWidth

val JustifyContentCenter = Css.JustifyContent.of(JustifyContent.center)

val AlignItemsCenter = AlignItems.of(Align.center)
val AlignItemsStretch = AlignItems.of(Align.stretch)
val AlignItemsStart = AlignItems.of(Align.flexStart)
val AlignItemsEnd = AlignItems.of(Align.flexEnd)
val AlignContentStart = AlignItems.of(Align.flexStart)

val Height2Px = Height.of(2.px)

val Flex1 = Css.Flex.of(Flex(1))