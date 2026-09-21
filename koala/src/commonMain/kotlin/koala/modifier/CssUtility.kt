package koala.modifier

import kotlinx.css.*

val Height get() = Css.Height
val MinHeight get() = Css.MinHeight
val MaxHeight get() = Css.MaxHeight
val Width get() = Css.Width
val MinWidth get() = Css.MinWidth
val MaxWidth get() = Css.MaxWidth

val Padding get() = Css.Padding
val PaddingTop get() = Css.PaddingTop
val PaddingRight get() = Css.PaddingRight
val PaddingBottom get() = Css.PaddingBottom
val PaddingLeft get() = Css.PaddingLeft
val Margin get() = Css.Margin
val MarginRight get() = Css.MarginRight
val MarginBottom get() = Css.MarginBottom
val MarginLeft get() = Css.MarginLeft
val MarginTop get() = Css.MarginTop
val Gap get() = Css.Gap
val FlexBasis get() = Css.FlexBasis
val FlexShrink get() = Css.FlexShrink

val JustifyContentCenter = Css.JustifyContent.of(JustifyContent.center)
val JustifyContentStart = Css.JustifyContent.of(JustifyContent.flexStart)
val JustifyContentEnd = Css.JustifyContent.of(JustifyContent.flexEnd)
val JustifyContentSpaceBetween = Css.JustifyContent.of(JustifyContent.spaceBetween)
val JustifyContentSpaceAround = Css.JustifyContent.of(JustifyContent.spaceAround)
val JustifyContentStretch = Css.JustifyContent.of(JustifyContent.stretch)

val TextAlignLeft = Css.TextAlign.of(TextAlign.left)
val TextAlignCenter = Css.TextAlign.of(TextAlign.center)
val TextAlignRight = Css.TextAlign.of(TextAlign.right)

val FlexDirectionRow = Css.FlexDirection.of(FlexDirection.row)
val FlexDirectionColumn = Css.FlexDirection.of(FlexDirection.column)
val FlexDirectionRowReverse = Css.FlexDirection.of(FlexDirection.rowReverse)
val FlexDirectionColumnReverse = Css.FlexDirection.of(FlexDirection.columnReverse)

val FlexWrap = Css.FlexWrap.of(kotlinx.css.FlexWrap.wrap)
val FlexWrapReverse = Css.FlexWrap.of(kotlinx.css.FlexWrap.wrapReverse)

val ObjectFitCover = Css.ObjectFit.of(ObjectFit.cover)
val ObjectFitContain = Css.ObjectFit.of(ObjectFit.contain)
val ObjectFitFill = Css.ObjectFit.of(ObjectFit.fill)
val ObjectFitScaleDown = Css.ObjectFit.of(ObjectFit.scaleDown)

val ObjectPositionCenter = Css.ObjectPosition.of("center")

val Aspect1 = Css.AspectRatio.of(1)
val Aspect2By1 = Css.AspectRatio.of(2)
val Aspect3By2 = Css.AspectRatio.of(1.5)
val Aspect3By1 = Css.AspectRatio.of(3)

val ZIndex1 = Css.ZIndex.of(1)
val ZIndex2 = Css.ZIndex.of(2)
val ZIndex3 = Css.ZIndex.of(3)

val ContainerTypeInlineSize = Css.ContainerType.of(ContainerType.inlineSize)

val Top0 = Css.Top.of(0.px)
val Left0 = Css.Left.of(0.px)
val Bottom0 = Css.Bottom.of(0.px)
val Right0 = Css.Right.of(0.px)

val PositionSticky = Css.Position.of(Position.sticky)
val PositionRelative = Css.Position.of(Position.relative)
val PositionAbsolute = Css.Position.of(Position.absolute)

val AlignItemsCenter = Css.AlignItems.of(Align.center)
val AlignItemsStretch = Css.AlignItems.of(Align.stretch)
val AlignItemsStart = Css.AlignItems.of(Align.flexStart)
val AlignItemsEnd = Css.AlignItems.of(Align.flexEnd)
val AlignContentStart = Css.AlignItems.of(Align.flexStart)

val AlignSelfStart = Css.AlignSelf.of(Align.start)
val AlignSelfCenter = Css.AlignSelf.of(Align.center)
val AlignSelfEnd = Css.AlignSelf.of(Align.end)
val AlignSelfStretch = Css.AlignSelf.of(Align.stretch)

val JustifySelfStart = Css.JustifySelf.of(JustifySelf.start)
val JustifySelfCenter = Css.JustifySelf.of(JustifySelf.center)
val JustifySelfEnd = Css.JustifySelf.of(JustifySelf.end)
val JustifySelfStretch = Css.JustifySelf.of(JustifySelf.stretch)

val PlaceSelfStart = Css.PlaceSelf.of(Align.start)
val PlaceSelfCenter = Css.PlaceSelf.of(Align.center)
val PlaceSelfStretch = Css.PlaceSelf.of(Align.stretch)
val PlaceItemsCenter = Css.PlaceItems.of(Align.center)
val PlaceContentEnd = Css.PlaceContent.of(JustifyContent.end)

val Margin1 = Margin(1)
val MarginAuto = Margin(LinearDimension.auto)

val Height2Px = Height.of(2.px)

val Flex0 = Css.Flex.of(Flex(0))
val Flex1 = Css.Flex.of(Flex(1))
val Flex2 = Css.Flex.of(Flex(2))
val Flex3 = Css.Flex.of(Flex(3))
val Flex4 = Css.Flex.of(Flex(4))

val Gap0 = Css.Gap(0)
val Gap2Px = Css.Gap.of(2.px)

val MinHeight0 = MinHeight(0)
val Height100Pct = Height(100.pct)
val Width100Pct = Width(100.pct)

val DisplayNone = Css.Display.of(Display.none)
val DisplayFlex = Css.Display.of(Display.flex)
val DisplayBlock = Css.Display.of(Display.block)
val DisplayInline = Css.Display.of(Display.inline)
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