package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s

// display utilities
object DisplayNone : Modifier { override val value = "display-none" }
object Opacity6: Modifier { override val value = "opacity-6" }
object Opacity4: Modifier { override val value = "opacity-4" }
object Opacity2: Modifier { override val value = "opacity-2" }
object Dim: Modifier { override val value = Opacity6.value }
object NoDim: Modifier { override val value = "no-dim" }
object Glow: Modifier { override val value = "glow" }
object GlowShadow: Modifier { override val value = "glow-shadow" }
object GlowBackground: Modifier { override val value = "glow-background" }
object CircleShape: Modifier { override val value = "circle-shape" }
object BorderRadius1: Modifier { override val value = "border-radius-1" }
object Accent: Modifier { override val value = "accent" }
object Primary: Modifier { override val value = "primary" }
object Secondary: Modifier { override val value = "secondary" }
object Clickable: Modifier { override val value = "clickable" }
object OverflowHidden: Modifier { override val value = "overflow-hidden"}
object OverflowWrapAnywhere: Modifier { override val value = "overflow-wrap-anywhere" }
object Focus: Modifier { override val value = "focus" }
object PrimaryBg: Modifier { override val value = "primary-bg" }
object PrimaryCardBg: Modifier { override val value = "primary-card-bg" }

// font utilities
object Bold: Modifier { override val value = "bold" }
object Italic: Modifier { override val value = "italic" }
object Large: Modifier { override val value = "large" }
object Heading1: Modifier { override val value = "heading-1" }
object Heading2: Modifier { override val value = "heading-2" }

// layout utilities
object Gap0: Modifier { override val value = "gap-0" }
object Gap1: Modifier { override val value = "gap-1" }
object Gap2: Modifier { override val value = "gap-2" }
object Gap4: Modifier { override val value = "gap-4" }
object Gap8: Modifier { override val value = "gap-8" }
object Flex1: Modifier { override val value = "flex-1" }
object Flex2: Modifier { override val value = "flex-2" }
object Flex3: Modifier { override val value = "flex-3" }
object Flex4: Modifier { override val value = "flex-4" }
object AlignItemsCenter: Modifier { override val value = "align-items-center" }
object AlignItemsStretch: Modifier { override val value = "align-items-stretch" }
object AlignItemsStart: Modifier { override val value = "align-items-start" }
object AlignItemsEnd: Modifier { override val value = "align-items-end" }
object JustifyCenter: Modifier { override val value = "justify-content-center" }
object JustifyEnd: Modifier { override val value = "justify-content-end" }
object JustifySpaceAround: Modifier { override val value = "justify-content-space-around" }
object JustifySpaceBetween: Modifier { override val value = "justify-content-space-between" }
object WidthAuto: Modifier { override val value = "width-auto" }
object Width100: Modifier { override val value = "width-100" }
object Width2: Modifier { override val value = "width-2" }
object Width4: Modifier { override val value = "width-4" }
object Width8: Modifier { override val value = "width-8" }
object Width16: Modifier { override val value = "width-16" }
object Width24: Modifier { override val value = "width-24" }
object Width32: Modifier { override val value = "width-32" }
object Width64: Modifier { override val value = "width-64" }
object Height100: Modifier { override val value = "height-100" }
object HeightAuto: Modifier { override val value = "height-auto" }
object Height2: Modifier { override val value = "height-2" }
object Height3: Modifier { override val value = "height-3" }
object Height4: Modifier { override val value = "height-4" }
object Height6: Modifier { override val value = "height-6" }
object Height8: Modifier { override val value = "height-8" }
object Height16: Modifier { override val value = "height-16" }
object Height32: Modifier { override val value = "height-32" }
object Height48: Modifier { override val value = "height-48" }
object MaxHeight32: Modifier { override val value = "max-height-32" }
object MaxHeight64: Modifier { override val value = "max-height-64" }
object MinHeight4: Modifier { override val value = "min-height-4" }
object MinHeight8: Modifier { override val value = "min-height-8" }
object MinHeight32: Modifier { override val value = "min-height-8" }
object Size100: Modifier { override val value = "size-100" }
object FillHeight: Modifier { override val value = "fill-height" }
object FlexItems1: Modifier { override val value = "flex-items-1" }
object FlexItemsBasis50: Modifier { override val value = "flex-items-basis-50" }
object TextAlignCenter: Modifier { override val value = "text-align-center"}
object TextAlignRight: Modifier { override val value = "text-align-right"}
object RowReverse: Modifier { override val value = "row-reverse" }
object QueryRowReverse: Modifier { override val value = "query-row-reverse" }
object MaxWidth25: Modifier { override val value = "max-width-25" }
object MaxWidth50: Modifier { override val value = "max-width-50" }
object MaxWidth64: Modifier { override val value = "max-width-64" }
object MarginAuto: Modifier { override val value = "margin-auto" }
object MarginTop1: Modifier { override val value = "margin-top-1" }
object MarginTop2: Modifier { override val value = "margin-top-2" }
object MarginTop4: Modifier { override val value = "margin-top-4" }
object MarginLeft1: Modifier { override val value = "margin-left-1" }
object SpaceBetween: Modifier { override val value = "space-between" }
object NoWrap: Modifier { override val value = "no-wrap" }
object WrapFlex: Modifier { override val value = "wrap-flex" }
object Square: Modifier { override val value = "square" }
object StackChildren: Modifier { override val value = "stack-children" }
object Start: Modifier { override val value = "start" }
object End: Modifier { override val value = "end" }
object Center: Modifier { override val value = "center" }
object CenterItems: Modifier { override val value = "center-items" }
object Padding1: Modifier { override val value = "padding-1" }
object Padding2: Modifier { override val value = "padding-2" }
