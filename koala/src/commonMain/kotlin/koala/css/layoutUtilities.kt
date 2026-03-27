package koala.css

val layoutUtilities
    get() = listOf(
        // Gap
        Gap0, Gap1, Gap2, Gap4, Gap8, GapTiny,
        // Flex
        Flex1, Flex2, Flex3, Flex4, FlexBasis25, FlexBasis33, RowReverse, QueryRowReverse, NoWrap, WrapFlex,
        // Align
        AlignItemsCenter, AlignItemsStretch, AlignItemsStart, AlignItemsEnd,
        JustifyContentCenter, JustifyContentEnd, JustifyContentSpaceAround, JustifyContentSpaceBetween,
        JustifySelfEnd, AlignSelfStart, AlignSelfStretch,
        // Width
        WidthAuto, Width100, WidthFitContent, Width2, Width4, Width8, Width12, Width16, Width24, Width32, Width64,
        MinWidth0, MinWidth16, MinWidth32, MinWidthAuto, MaxWidth25, MaxWidth50, MaxWidth64,
        // Height
        HeightAuto, Height100, Height2, Height3, Height4, Height5, Height6, Height8, Height12, Height16, Height24, Height32, Height48,
        MinHeightAuto, MinHeight0, MinHeight4, MinHeight8, MinHeight16, MinHeight24, MinHeight32,
        MaxHeight8, MaxHeight16, MaxHeight24, MaxHeight32, MaxHeight64,
        // Size
        Size100, FillHeight, Square,
        // Flex Items
        FlexItems1, FlexItemsBasis50,
        // Text
        TextAlignCenter, TextAlignRight,
        // Margin
        MarginAuto, Margin1, Margin2, MarginTop1, MarginTop2, MarginTop4, MarginTop8, MarginLeft1,
        // Padding
        Padding0, Padding1, Padding2, PaddingTiny,
        // Other
        SpaceBetween, Start, End, Center, PlaceItemsCenter, RelativeParent, ZIndex1, Shrinkable
    )

// Gap
val Gap0 = CssUtility("gap-0", ".gap-0 { gap: 0; }")
val Gap1 = CssUtility("gap-1", ".gap-1 { gap: var(--unit-spacing); }")
val Gap2 = CssUtility("gap-2", ".gap-2 { gap: calc(var(--unit-spacing) * 2); }")
val Gap4 = CssUtility("gap-4", ".gap-4 { gap: calc(var(--unit-spacing) * 4); }")
val Gap8 = CssUtility("gap-8", ".gap-8 { gap: calc(var(--unit-spacing) * 8); }")
val GapTiny = CssUtility("gap-tiny", ".gap-tiny { gap: 2px; }")

// Flex
val Flex1 = CssUtility("flex-1", ".flex-1 { flex: 1; }")
val Flex2 = CssUtility("flex-2", ".flex-2 { flex: 2; }")
val Flex3 = CssUtility("flex-3", ".flex-3 { flex: 3; }")
val Flex4 = CssUtility("flex-4", ".flex-4 { flex: 4; }")

// Flex Basis
val FlexBasis25 = CssUtility("flex-basis-25", ".flex-basis-25 { flex-basis: 25%; }")
val FlexBasis33 = CssUtility("flex-basis-33", ".flex-basis-33 { flex-basis: 33%; }")

// Align Items
val AlignItemsCenter = CssUtility("align-items-center", ".align-items-center { align-items: center; }")
val AlignItemsStretch = CssUtility("align-items-stretch", ".align-items-stretch { align-items: stretch; }")
val AlignItemsStart = CssUtility("align-items-start", ".align-items-start { align-items: flex-start; }")
val AlignItemsEnd = CssUtility("align-items-end", ".align-items-end { align-items: flex-end; }")

// Justify Content
val JustifyContentCenter = CssUtility("justify-content-center", ".justify-content-center { justify-content: center; }")
val JustifyContentEnd = CssUtility("justify-content-end", ".justify-content-end { justify-content: flex-end; }")
val JustifyContentSpaceAround =
    CssUtility("justify-content-space-around", ".justify-content-space-around { justify-content: space-around; }")
val JustifyContentSpaceBetween =
    CssUtility("justify-content-space-between", ".justify-content-space-between { justify-content: space-between; }")

// Width
val WidthAuto = CssUtility("width-auto", ".width-auto { width: auto; }")
val Width100 = CssUtility("width-100", ".width-100 { width: 100%; }")
val WidthFitContent = CssUtility("width-fit-content", ".width-fit-content { width: fit-content; }")
val Width2 = CssUtility("width-2", ".width-2 { width: calc(var(--unit-spacing) * 2); }")
val Width4 = CssUtility("width-4", ".width-4 { width: calc(var(--unit-spacing) * 4); }")
val Width8 = CssUtility("width-8", ".width-8 { width: calc(var(--unit-spacing) * 8); }")
val Width12 = CssUtility("width-12", ".width-12 { width: calc(var(--unit-spacing) * 12); }")
val Width16 = CssUtility("width-16", ".width-16 { width: calc(var(--unit-spacing) * 16); }")
val Width24 = CssUtility("width-24", ".width-24 { width: calc(var(--unit-spacing) * 24); }")
val Width32 = CssUtility("width-32", ".width-32 { width: calc(var(--unit-spacing) * 32); }")
val Width64 = CssUtility("width-64", ".width-64 { width: calc(var(--unit-spacing) * 64); }")
val MinWidth0 = CssUtility("min-width-0", ".min-width-0 { min-width: 0; }")
val MinWidth16 = CssUtility("min-width-16", ".min-width-16 { min-width: calc(var(--unit-spacing) * 16); }")
val MinWidth32 = CssUtility("min-width-32", ".min-width-32 { min-width: calc(var(--unit-spacing) * 32); }")
val MinWidthAuto = CssUtility("min-width-auto", ".min-width-auto { min-width: auto; }")

// Height
val Height100 = CssUtility("height-100", ".height-100 { height: 100%; }")
val HeightAuto = CssUtility("height-auto", ".height-auto { height: auto; }")
val Height2 = CssUtility("height-2", ".height-2 { height: calc(var(--unit-spacing) * 2); }")
val Height3 = CssUtility("height-3", ".height-3 { height: calc(var(--unit-spacing) * 3); }")
val Height4 = CssUtility("height-4", ".height-4 { height: calc(var(--unit-spacing) * 4); }")
val Height5 = CssUtility("height-5", ".height-5 { height: calc(var(--unit-spacing) * 5); }")
val Height6 = CssUtility("height-6", ".height-6 { height: calc(var(--unit-spacing) * 6); }")
val Height8 = CssUtility("height-8", ".height-8 { height: calc(var(--unit-spacing) * 8); }")
val Height12 = CssUtility("height-12", ".height-12 { height: calc(var(--unit-spacing) * 12); }")
val Height16 = CssUtility("height-16", ".height-16 { height: calc(var(--unit-spacing) * 16); }")
val Height24 = CssUtility("height-24", ".height-24 { height: calc(var(--unit-spacing) * 24); }")
val Height32 = CssUtility("height-32", ".height-32 { height: calc(var(--unit-spacing) * 32); }")
val Height48 = CssUtility("height-48", ".height-48 { height: calc(var(--unit-spacing) * 48); }")
val MaxHeight8 = CssUtility("max-height-8", ".max-height-8 { max-height: calc(var(--unit-spacing) * 8); }")
val MaxHeight16 = CssUtility("max-height-16", ".max-height-16 { max-height: calc(var(--unit-spacing) * 16); }")
val MaxHeight24 = CssUtility("max-height-24", ".max-height-24 { max-height: calc(var(--unit-spacing) * 24); }")
val MaxHeight32 = CssUtility("max-height-32", ".max-height-32 { max-height: calc(var(--unit-spacing) * 32); }")
val MaxHeight64 = CssUtility("max-height-64", ".max-height-64 { max-height: calc(var(--unit-spacing) * 64); }")
val MinHeightAuto = CssUtility("min-height-auto", ".min-height-auto { min-height: auto; }")
val MinHeight0 = CssUtility("min-height-0", ".min-height-0 { min-height: 0; }")
val MinHeight4 = CssUtility("min-height-4", ".min-height-4 { min-height: calc(var(--unit-spacing) * 4); }")
val MinHeight8 = CssUtility("min-height-8", ".min-height-8 { min-height: calc(var(--unit-spacing) * 8); }")
val MinHeight16 = CssUtility("min-height-16", ".min-height-16 { min-height: calc(var(--unit-spacing) * 16); }")
val MinHeight24 = CssUtility("min-height-24", ".min-height-24 { min-height: calc(var(--unit-spacing) * 24); }")
val MinHeight32 = CssUtility("min-height-32", ".min-height-32 { min-height: calc(var(--unit-spacing) * 32); }")

// Size
val Size100 = CssUtility("size-100", ".size-100 { width: 100%; height: 100%; }")
val FillHeight = CssUtility("fill-height", ".fill-height { height: auto; max-height: 100%; }")

// Flex Items
val FlexItems1 = CssUtility("flex-items-1", ".flex-items-1 > * { flex: 1; }")
val FlexItemsBasis50 = CssUtility("flex-items-basis-50", ".flex-items-basis-50 > * { flex-basis: 50%; }")

// Text
val TextAlignCenter = CssUtility("text-align-center", ".text-align-center { text-align: center; }")
val TextAlignRight = CssUtility("text-align-right", ".text-align-right { text-align: right; }")

// Row / Wrap
val RowReverse = CssUtility("row-reverse", ".row-reverse { flex-direction: row-reverse; }")
val QueryRowReverse = CssUtility("query-row-reverse", null)
val NoWrap = CssUtility("no-wrap", ".no-wrap { flex-wrap: nowrap; white-space: nowrap; }")
val WrapFlex = CssUtility("wrap-flex", ".wrap-flex { flex-wrap: wrap; }")
val MaxWidth25 = CssUtility("max-width-25", ".max-width-25 { max-width: 25%; }")
val MaxWidth50 = CssUtility("max-width-50", ".max-width-50 { max-width: 50%; }")
val MaxWidth64 = CssUtility("max-width-64", ".max-width-64 { max-width: calc(var(--unit-spacing) * 64); }")

// Margin
val MarginAuto = CssUtility("margin-auto", ".margin-auto { margin: auto; }")
val Margin1 = CssUtility("margin-1", ".margin-1 { margin: var(--unit-spacing); }")
val Margin2 = CssUtility("margin-2", ".margin-2 { margin: calc(var(--unit-spacing) * 2); }")
val MarginTop1 = CssUtility("margin-top-1", ".margin-top-1 { margin-top: var(--unit-spacing); }")
val MarginTop2 = CssUtility("margin-top-2", ".margin-top-2 { margin-top: calc(var(--unit-spacing) * 2); }")
val MarginTop4 = CssUtility("margin-top-4", ".margin-top-4 { margin-top: calc(var(--unit-spacing) * 4); }")
val MarginTop8 = CssUtility("margin-top-8", ".margin-top-8 { margin-top: calc(var(--unit-spacing) * 8); }")
val MarginLeft1 = CssUtility("margin-left-1", ".margin-left-1 { margin-left: var(--unit-spacing); }")

// Padding
val Padding0 = CssUtility("padding-0", ".padding-0 { padding: 0; }")
val Padding1 = CssUtility("padding-1", ".padding-1 { padding: var(--unit-spacing); }")
val Padding2 = CssUtility("padding-2", ".padding-2 { padding: calc(var(--unit-spacing) * 2); }")
val PaddingTiny = CssUtility("padding-tiny", ".padding-tiny { padding: 2px; }")

// Other
val SpaceBetween = CssUtility("space-between", ".space-between { justify-content: space-between; }")
val Start = CssUtility("start", null)
val End = CssUtility("end", null)
val Center = CssUtility("center", null)
val PlaceItemsCenter = CssUtility("center-items", null)
val JustifySelfEnd = CssUtility("justify-self-end", ".justify-self-end { justify-self: end; }")
val AlignSelfStart = CssUtility("align-self-start", ".align-self-start { align-self: start; }")
val AlignSelfStretch = CssUtility("align-self-stretch", ".align-self-stretch { align-self: stretch; }")
val Square = CssUtility("square", ".square { aspect-ratio: 1 / 1; }")
val RelativeParent = CssUtility(
    "relative-parent",
    ".relative-parent { position: relative; } .relative-parent > * { position: absolute; inset: 0; }"
)
val ZIndex1 = CssUtility("z-index-1", ".z-index-1 { z-index: 1; }")
val Shrinkable = CssUtility("shrinkable", null)

// layout utilities
// object Gap0: Modifier { override val identifier = "gap-0" }
// object Gap1: Modifier { override val identifier = "gap-1" }
// object Gap2: Modifier { override val identifier = "gap-2" }
// object Gap4: Modifier { override val identifier = "gap-4" }
// object Gap8: Modifier { override val identifier = "gap-8" }
// object GapTiny: Modifier { override val identifier = "gap-tiny" }
// object Flex1: Modifier { override val identifier = "flex-1" }
// object Flex2: Modifier { override val identifier = "flex-2" }
// object Flex3: Modifier { override val identifier = "flex-3" }
// object Flex4: Modifier { override val identifier = "flex-4" }
// object FlexBasis25: Modifier { override val identifier = "flex-basis-25" }
// object FlexBasis33: Modifier { override val identifier = "flex-basis-33" }
// object AlignItemsCenter: Modifier { override val identifier = "align-items-center" }
// object AlignItemsStretch: Modifier { override val identifier = "align-items-stretch" }
// object AlignItemsStart: Modifier { override val identifier = "align-items-start" }
// object AlignItemsEnd: Modifier { override val identifier = "align-items-end" }
// object JustifyContentCenter: Modifier { override val identifier = "justify-content-center" }
// object JustifyEnd: Modifier { override val identifier = "justify-content-end" }
// object JustifySpaceAround: Modifier { override val identifier = "justify-content-space-around" }
// object JustifySpaceBetween: Modifier { override val identifier = "justify-content-space-between" }
// object WidthAuto: Modifier { override val identifier = "width-auto" }
// object Width100: Modifier { override val identifier = "width-100" }
// object WidthFitContent: Modifier { override val identifier = "width-fit-content" }
// object Width2: Modifier { override val identifier = "width-2" }
// object Width4: Modifier { override val identifier = "width-4" }
// object Width8: Modifier { override val identifier = "width-8" }
// object Width12: Modifier { override val identifier = "width-12" }
// object Width16: Modifier { override val identifier = "width-16" }
// object Width24: Modifier { override val identifier = "width-24" }
// object Width32: Modifier { override val identifier = "width-32" }
// object Width64: Modifier { override val identifier = "width-64" }
// object MinWidth0: Modifier { override val identifier = "min-width-0" }
// object MinWidth16: Modifier { override val identifier = "min-width-16" }
// object MinWidth32: Modifier { override val identifier = "min-width-32" }
// object MinWidthAuto: Modifier { override val identifier = "min-width-auto" }
// object Height100: Modifier { override val identifier = "height-100" }
// object HeightAuto: Modifier { override val identifier = "height-auto" }
// object Height2: Modifier { override val identifier = "height-2" }
// object Height3: Modifier { override val identifier = "height-3" }
// object Height4: Modifier { override val identifier = "height-4" }
// object Height5: Modifier { override val identifier = "height-5" }
// object Height6: Modifier { override val identifier = "height-6" }
// object Height8: Modifier { override val identifier = "height-8" }
// object Height12: Modifier { override val identifier = "height-12" }
// object Height16: Modifier { override val identifier = "height-16" }
// object Height24: Modifier { override val identifier = "height-24" }
// object Height32: Modifier { override val identifier = "height-32" }
// object Height48: Modifier { override val identifier = "height-48" }
// object MaxHeight8: Modifier { override val identifier = "max-height-8" }
// object MaxHeight16: Modifier { override val identifier = "max-height-16" }
// object MaxHeight24: Modifier { override val identifier = "max-height-24" }
// object MaxHeight32: Modifier { override val identifier = "max-height-32" }
// object MaxHeight64: Modifier { override val identifier = "max-height-64" }
// object MinHeightAuto: Modifier { override val identifier = "min-height-auto" }
// object MinHeight0: Modifier { override val identifier = "min-height-0" }
// object MinHeight4: Modifier { override val identifier = "min-height-4" }
// object MinHeight8: Modifier { override val identifier = "min-height-8" }
// object MinHeight16: Modifier { override val identifier = "min-height-16" }
// object MinHeight24: Modifier { override val identifier = "min-height-24" }
// object MinHeight32: Modifier { override val identifier = "min-height-32" }
// object Size100: Modifier { override val identifier = "size-100" }
// object FillHeight: Modifier { override val identifier = "fill-height" }
// object FlexItems1: Modifier { override val identifier = "flex-items-1" }
// object FlexItemsBasis50: Modifier { override val identifier = "flex-items-basis-50" }
// object TextAlignCenter: Modifier { override val identifier = "text-align-center"}
// object TextAlignRight: Modifier { override val identifier = "text-align-right"}
// object RowReverse: Modifier { override val identifier = "row-reverse" }
// object QueryRowReverse: Modifier { override val identifier = "query-row-reverse" }
// object MaxWidth25: Modifier { override val identifier = "max-width-25" }
// object MaxWidth50: Modifier { override val identifier = "max-width-50" }
// object MaxWidth64: Modifier { override val identifier = "max-width-64" }
// object MarginAuto: Modifier { override val identifier = "margin-auto" }
// object MarginTop1: Modifier { override val identifier = "margin-top-1" }
// object MarginTop2: Modifier { override val identifier = "margin-top-2" }
// object MarginTop4: Modifier { override val identifier = "margin-top-4" }
// object MarginLeft1: Modifier { override val identifier = "margin-left-1" }
// object SpaceBetween: Modifier { override val identifier = "space-between" }
// object NoWrap: Modifier { override val identifier = "no-wrap" }
// object WrapFlex: Modifier { override val identifier = "wrap-flex" }
// object Square: Modifier { override val identifier = "square" }
// object StackChildren: Modifier { override val identifier = "stack-children" }
// object Start: Modifier { override val identifier = "start" }
// object End: Modifier { override val identifier = "end" }
// object Center: Modifier { override val identifier = "center" }
// object CenterItems: Modifier { override val identifier = "center-items" }
// object Padding0: Modifier { override val identifier = "padding-0" }
// object Padding1: Modifier { override val identifier = "padding-1" }
// object Padding2: Modifier { override val identifier = "padding-2" }
// object PaddingTiny: Modifier { override val identifier = "padding-tiny" }
// object Margin1: Modifier { override val identifier = "margin-1" }
// object Margin2: Modifier { override val identifier = "margin-2" }
// object JustifySelfEnd: Modifier { override val identifier = "justify-self-end" }
// object AlignSelfStart: Modifier { override val identifier = "align-self-start" }
// object AlignSelfStretch: Modifier { override val identifier = "align-self-stretch" }
// object RelativeParent: Modifier { override val identifier = "relative-parent" }
// object ZIndex1: Modifier { override val identifier = "z-index-1" }
// object Shrinkable: Modifier { override val identifier = "shrinkable" }