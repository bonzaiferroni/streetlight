package koala.modifier

import kotlinx.css.*

val Height get() = Css.Height
val Padding get() = Css.Padding
val Margin get() = Css.Margin
val Gap get() = Css.Gap
val MaxWidth get() = Css.MaxWidth

val JustifyContentCenter = Css.JustifyContent.of(JustifyContent.center)

val AlignItemsCenter = Css.AlignItems.of(Align.center)
val AlignItemsStretch = Css.AlignItems.of(Align.stretch)
val AlignItemsStart = Css.AlignItems.of(Align.flexStart)
val AlignItemsEnd = Css.AlignItems.of(Align.flexEnd)
val AlignContentStart = Css.AlignItems.of(Align.flexStart)

val Height2Px = Height.of(2.px)

val Flex1 = Css.Flex.of(Flex(1))

val DisplayNone = Css.Display.of(Display.none)
val VisibilityHidden = Css.Visibility.of(Visibility.hidden)