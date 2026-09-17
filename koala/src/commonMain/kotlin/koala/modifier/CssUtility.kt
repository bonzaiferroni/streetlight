package koala.modifier

import kotlinx.css.*

val Height get() = Css.Height
val MinHeight get() = Css.MinHeight
val MaxHeight get() = Css.MaxHeight
val Width get() = Css.Width
val MinWidth get() = Css.MinWidth
val MaxWidth get() = Css.MaxWidth

val Padding get() = Css.Padding
val Margin get() = Css.Margin
val Gap get() = Css.Gap

val JustifyContentCenter = Css.JustifyContent.of(JustifyContent.center)

val AlignItemsCenter = Css.AlignItems.of(Align.center)
val AlignItemsStretch = Css.AlignItems.of(Align.stretch)
val AlignItemsStart = Css.AlignItems.of(Align.flexStart)
val AlignItemsEnd = Css.AlignItems.of(Align.flexEnd)
val AlignContentStart = Css.AlignItems.of(Align.flexStart)

val Height2Px = Height.of(2.px)

val Flex1 = Css.Flex.of(Flex(1))

val Gap0 = Css.Gap(0)
val Gap2Px = Css.Gap.of(2.px)

val MinHeight0 = MinHeight(0)
val Height100Pct = Height(100.pct)
val Width100Pct = Width(100.pct)

val DisplayNone = Css.Display.of(Display.none)
val VisibilityHidden = Css.Visibility.of(Visibility.hidden)

val Opacity0 = Css.Opacity.of(0)
val Opacity1 = Css.Opacity.of(1)

val BorderRadius0 = Css.BorderRadius(0)
val BorderRadius1 = Css.BorderRadius(1)
val BorderRadius2 = Css.BorderRadius(2)
val BorderRadius3 = Css.BorderRadius(3)
val BorderRadius4 = Css.BorderRadius(4)
val BorderRadius50P = Css.BorderRadius(50.pct)
val BorderRadiusPill = Css.BorderRadius(9999.px)
val BorderRadiusPillLeft = Css.BorderRadius(LinearDimension("9999px 0 0 9999px"))

val MaxWidthTextBody = MaxWidth(108)

//val Height100Vh = utilityOf("height-100vh", "height: 100dvh")
val SmallIconHeight = Height(3)
val LargeIconHeight = Height(5)