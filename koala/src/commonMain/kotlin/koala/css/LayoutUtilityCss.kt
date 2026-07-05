package koala.css

val LayoutUtilityCss
    get() = listOf(
        // Gap
        Gap0, Gap1, Gap2, Gap3, Gap4, Gap8, GapTiny, GapHalf,
        // Flex
        Flex0, Flex1, Flex2, Flex3, Flex4, FlexMd1, FlexMd2, FlexColumn, FlexBasis25, FlexBasis33, FlexShrink0,
        FlexDirectionRow, FlexDirectionRowReverse, QueryRowReverse, NoWrap, FlexWrap, FlexBasisMin, DisplayFlex,
        // Align
        AlignItemsCenter, AlignItemsStretch, AlignItemsStart, AlignItemsEnd,
        JustifyContentCenter, JustifyContentEnd, JustifyContentSpaceAround, JustifyContentSpaceBetween,
        JustifySelfEnd, JustifySelfCenter, JustifyContentStretch, JustifyContentStart,
        AlignSelfStart, AlignSelfCenter, AlignSelfEnd, AlignSelfStretch, JustifySelfStart, JustifySelfStretch,
        // Width
        WidthAuto, Width100P, WidthFitContent,
        Width1, Width2, Width4, Width5, Width8, Width10, Width12, Width16, Width24, Width32, Width64,
        MinWidth0, MinWidth12, MinWidth6, MinWidth8, MinWidth14, MinWidth16, MinWidth24, MinWidth32, MinWidth36, MinWidth48, MinWidthAuto, MaxWidth25P, MaxWidth50P,
        MaxWidth5, MaxWidth16, MaxWidth24, MaxWidth32, MaxWidth48, MaxWidth64, MaxWidthTextBody, MaxWidthNone,
        // Height
        HeightAuto, Height50P, Height100P,
        Height0, Height2, Height2Px, Height3, Height4, Height5, Height6, Height7, Height8, Height9, Height10, Height12, Height16,
        Height24, Height32, Height48,
        MinHeightAuto, MinHeight0, MinHeight4, MinHeight5, MinHeight6, MinHeight8, MinHeight16, MinHeight24, MinHeight32, MinHeight48, MinHeight100P,
        MaxHeight5, MaxHeight8, MaxHeight12, MaxHeight16, MaxHeight24, MaxHeight32, MaxHeight64,
        // Size
        Size100P, FillHeight, Aspect1, Aspect2By1, Aspect3By1, Aspect3By2,
        // Flex Items
        FlexItems1, FlexItemsEqual, FlexItemsBasis50,
        // Text
        TextAlignCenter, TextAlignRight,
        // Margin
        MarginAuto, Margin1, Margin2, MarginTop1, MarginTop2, MarginTopTiny, MarginTop4, MarginTop8,
        MarginLeft1, MarginLeft2, MarginLeftAuto,
        MarginRight1, MarginRight2, MarginRight4Px, MarginBottom1, MarginBottom2, MarginBottom16,
        MarginX1, MarginX2, MarginX4,
        // Padding
        Padding0, Padding1, Padding2, Padding4, PaddingTiny, PaddingLeft1, PaddingLeft2, PaddingLeft3, PaddingRight3,
        PaddingTop1, Padding2Px,
        PaddingX1, PaddingX2, PaddingY1, PaddingY2, PaddingBottom1,
        // Object Fit
        ObjectFitCover, ObjectFitContain, ObjectPositionCenter, ObjectFitFill,
        // Position
        PositionSticky, PositionRelative, PositionAbsolute, Top0, Right0, Bottom0, Left0,
        // Other
        SpaceBetween, Start, End, PlaceSelfCenter, PlaceItemsCenter, RelativeParent, ZIndex1, ZIndex2, ZIndex3, Shrinkable, QueryContainer,
        TopSpacing1, TopSpacing8,
    )

// Gap
val Gap0 = utilityOf("gap-0", "gap: 0")
val Gap1 = utilityOf("gap-1", "gap: var(--unit-spacing)")
val Gap2 = utilityOf("gap-2", "gap: var(--unit-spacing-2)")
val Gap3 = utilityOf("gap-3", "gap: var(--unit-spacing-3)")
val Gap4 = utilityOf("gap-4", "gap: var(--unit-spacing-4)")
val Gap8 = utilityOf("gap-8", "gap: var(--unit-spacing-8)")
val GapTiny = utilityOf("gap-tiny", "gap: 2px")
val GapHalf = utilityOf("gap-half", "gap: calc(var(--unit-spacing) / 2)")

// Flex
val Flex0 = utilityOf("flex-0", "flex: 0")
val Flex1 = utilityOf("flex-1", "flex: 1")
val Flex2 = utilityOf("flex-2", "flex: 2")
val Flex3 = utilityOf("flex-3", "flex: 3")
val Flex4 = utilityOf("flex-4", "flex: 4")
val FlexMd1 = CssUtility("flex-md-1")
val FlexMd2 = CssUtility("flex-md-2")
val FlexColumn = CssUtility("column")
val FlexShrink0 = utilityOf("flex-grow", "flex-shrink: 0")

// Flex Basis
val FlexBasis25 = utilityOf("flex-basis-25", "flex-basis: 25%")
val FlexBasis33 = utilityOf("flex-basis-33", "flex-basis: 33%")

// Align Items
val AlignItemsCenter = utilityOf("align-items-center", "align-items: center")
val AlignItemsStretch = utilityOf("align-items-stretch", "align-items: stretch")
val AlignItemsStart = utilityOf("align-items-start", "align-items: flex-start")
val AlignItemsEnd = utilityOf("align-items-end", "align-items: flex-end")

// Justify Content
val JustifyContentCenter = utilityOf("justify-content-center", "justify-content: center")
val JustifyContentEnd = utilityOf("justify-content-end", "justify-content: flex-end")
val JustifyContentSpaceAround = utilityOf("justify-content-space-around", "justify-content: space-around")
val JustifyContentSpaceBetween = utilityOf("justify-content-space-between", "justify-content: space-between")
val JustifyContentStretch = utilityOf("justify-content-stretch", "justify-content: stretch")
val JustifyContentStart = utilityOf("justify-content-start", "justify-content: flex-start")

// Width
val WidthAuto = utilityOf("width-auto", "width: auto")
val Width100P = utilityOf("width-100", "width: 100%")
val WidthFitContent = utilityOf("width-fit-content", "width: fit-content")
val Width1 = utilityOf("width-1", "width: var(--unit-spacing)")
val Width2 = utilityOf("width-2", "width: calc(var(--unit-spacing) * 2)")
val Width4 = utilityOf("width-4", "width: calc(var(--unit-spacing) * 4)")
val Width5 = utilityOf("width-5", "width: calc(var(--unit-spacing) * 5)")
val Width8 = utilityOf("width-8", "width: calc(var(--unit-spacing) * 8)")
val Width10 = utilityOf("width-10", "width: calc(var(--unit-spacing) * 10)")
val Width12 = utilityOf("width-12", "width: calc(var(--unit-spacing) * 12)")
val Width16 = utilityOf("width-16", "width: calc(var(--unit-spacing) * 16)")
val Width24 = utilityOf("width-24", "width: calc(var(--unit-spacing) * 24)")
val Width32 = utilityOf("width-32", "width: calc(var(--unit-spacing) * 32)")
val Width64 = utilityOf("width-64", "width: calc(var(--unit-spacing) * 64)")
val MinWidth0 = utilityOf("min-width-0", "min-width: 0")
val MinWidth6 = utilityOf("min-width-6", "min-width: calc(var(--unit-spacing) * 6)")
val MinWidth8 = utilityOf("min-width-16", "min-width: calc(var(--unit-spacing) * 8)")
val MinWidth12 = utilityOf("min-width-12", "min-width: calc(var(--unit-spacing) * 12)")
val MinWidth14 = utilityOf("min-width-14", "min-width: calc(var(--unit-spacing) * 14)")
val MinWidth16 = utilityOf("min-width-16", "min-width: calc(var(--unit-spacing) * 16)")
val MinWidth24 = utilityOf("min-width-24", "min-width: calc(var(--unit-spacing) * 24)")
val MinWidth32 = utilityOf("min-width-32", "min-width: calc(var(--unit-spacing) * 32)")
val MinWidth36 = utilityOf("min-width-36", "min-width: calc(var(--unit-spacing) * 36)")
val MinWidth48 = utilityOf("min-width-48", "min-width: calc(var(--unit-spacing) * 48)")
val MinWidthAuto = utilityOf("min-width-auto", "min-width: auto")
val MaxWidth25P = utilityOf("max-width-25", "max-width: 25%")
val MaxWidth50P = utilityOf("max-width-50", "max-width: 50%")
val MaxWidth5 = utilityOf("max-width-5", "max-width: calc(var(--unit-spacing) * 5)")
val MaxWidth16 = utilityOf("max-width-16", "max-width: calc(var(--unit-spacing) * 16)")
val MaxWidth24 = utilityOf("max-width-24", "max-width: calc(var(--unit-spacing) * 24)")
val MaxWidth32 = utilityOf("max-width-32", "max-width: calc(var(--unit-spacing) * 32)")
val MaxWidth48 = utilityOf("max-width-48", "max-width: calc(var(--unit-spacing) * 48)")
val MaxWidth64 = utilityOf("max-width-64", "max-width: calc(var(--unit-spacing) * 64)")
val MaxWidthTextBody = utilityOf("max-width-text-body", "max-width: calc(var(--unit-spacing) * 108)" )
val MaxWidthNone = utilityOf("max-width-none", "max-width: none")

// Height
val Height50P = utilityOf("height-50", "height: 50%")
val Height100P = utilityOf("height-100", "height: 100%")
val HeightAuto = utilityOf("height-auto", "height: auto")
val Height0 = utilityOf("height-0", "height: 0")
val Height2 = utilityOf("height-2", "height: calc(var(--unit-spacing) * 2)")
val Height2Px = utilityOf("height-2px", "height: 2px")
val Height3 = utilityOf("height-3", "height: calc(var(--unit-spacing) * 3)")
val Height4 = utilityOf("height-4", "height: calc(var(--unit-spacing) * 4)")
val Height5 = utilityOf("height-5", "height: calc(var(--unit-spacing) * 5)")
val Height6 = utilityOf("height-6", "height: calc(var(--unit-spacing) * 6)")
val Height7 = utilityOf("height-7", "height: calc(var(--unit-spacing) * 7)")
val Height8 = utilityOf("height-8", "height: calc(var(--unit-spacing) * 8)")
val Height9 = utilityOf("height-9", "height: calc(var(--unit-spacing) * 9)")
val Height10 = utilityOf("height-10", "height: calc(var(--unit-spacing) * 10)")
val Height12 = utilityOf("height-12", "height: calc(var(--unit-spacing) * 12)")
val Height16 = utilityOf("height-16", "height: calc(var(--unit-spacing) * 16)")
val Height24 = utilityOf("height-24", "height: calc(var(--unit-spacing) * 24)")
val Height32 = utilityOf("height-32", "height: calc(var(--unit-spacing) * 32)")
val Height48 = utilityOf("height-48", "height: calc(var(--unit-spacing) * 48)")
val MaxHeight5 = utilityOf("max-height-5", "max-height: calc(var(--unit-spacing) * 5)")
val MaxHeight8 = utilityOf("max-height-8", "max-height: calc(var(--unit-spacing) * 8)")
val MaxHeight12 = utilityOf("max-height-12", "max-height: calc(var(--unit-spacing) * 12)")
val MaxHeight16 = utilityOf("max-height-16", "max-height: calc(var(--unit-spacing) * 16)")
val MaxHeight24 = utilityOf("max-height-24", "max-height: calc(var(--unit-spacing) * 24)")
val MaxHeight32 = utilityOf("max-height-32", "max-height: calc(var(--unit-spacing) * 32)")
val MaxHeight64 = utilityOf("max-height-64", "max-height: calc(var(--unit-spacing) * 64)")
val MinHeightAuto = utilityOf("min-height-auto", "min-height: auto")
val MinHeight0 = utilityOf("min-height-0", "min-height: 0")
val MinHeight4 = utilityOf("min-height-4", "min-height: calc(var(--unit-spacing) * 4)")
val MinHeight5 = utilityOf("min-height-5", "min-height: calc(var(--unit-spacing) * 5)")
val MinHeight6 = utilityOf("min-height-6", "min-height: calc(var(--unit-spacing) * 6)")
val MinHeight8 = utilityOf("min-height-8", "min-height: calc(var(--unit-spacing) * 8)")
val MinHeight16 = utilityOf("min-height-16", "min-height: calc(var(--unit-spacing) * 16)")
val MinHeight24 = utilityOf("min-height-24", "min-height: calc(var(--unit-spacing) * 24)")
val MinHeight32 = utilityOf("min-height-32", "min-height: calc(var(--unit-spacing) * 32)")
val MinHeight48 = utilityOf("min-height-48", "min-height: calc(var(--unit-spacing) * 48)")
val MinHeight100P = utilityOf("min-height-100", "min-height: 100%")

// Size
val Size100P = utilityOf("size-100", "width: 100%", "height: 100%")
val FillHeight = utilityOf("fill-height", "height: auto", "max-height: 100%")

// Flex Items
val FlexItems1 = CssUtility("flex-items-1", ".flex-items-1 > * { flex: 1; }")
val FlexItemsEqual = CssUtility("flex-items-equal", ".flex-items-equal > * { flex: 1 1 0; min-width: 0; }")
val FlexItemsBasis50 = CssUtility("flex-items-basis-50", ".flex-items-basis-50 > * { flex-basis: 50%; }")

// Text
val TextAlignCenter = utilityOf("text-align-center", "text-align: center")
val TextAlignRight = utilityOf("text-align-right", "text-align: right")

// Row / Wrap
val FlexDirectionRowReverse = utilityOf("row-reverse", "flex-direction: row-reverse")
val FlexDirectionRow = utilityOf("flex-direction-row", "flex-direction: row")
val QueryRowReverse = CssUtility("query-row-reverse")
val NoWrap = utilityOf("no-wrap", "flex-wrap: nowrap", "white-space: nowrap")
val FlexWrap = utilityOf("flex-wrap", "flex-wrap: wrap")
val FlexBasisMin = utilityOf("flex-basis-min", "flex-basis: min-content")
val DisplayFlex = utilityOf("display-flex", "display: flex")

// Margin
val MarginAuto = utilityOf("margin-auto", "margin: auto")
val Margin1 = utilityOf("margin-1", "margin: var(--unit-spacing)")
val Margin2 = utilityOf("margin-2", "margin: calc(var(--unit-spacing) * 2)")
val MarginTop1 = utilityOf("margin-top-1", "margin-top: var(--unit-spacing)")
val MarginTopTiny = utilityOf("margin-top-2px", "margin-top: 2px")
val MarginTop2 = utilityOf("margin-top-2", "margin-top: calc(var(--unit-spacing) * 2)")
val MarginTop4 = utilityOf("margin-top-4", "margin-top: calc(var(--unit-spacing) * 4)")
val MarginTop8 = utilityOf("margin-top-8", "margin-top: calc(var(--unit-spacing) * 8)")
val MarginLeft1 = utilityOf("margin-left-1", "margin-left: var(--unit-spacing)")
val MarginLeft2 = utilityOf("margin-left-2", "margin-left: var(--unit-spacing-2)")
val MarginLeftAuto = utilityOf("margin-left-auto", "margin-left: auto")
val MarginRight1 = utilityOf("margin-right-1", "margin-right: var(--unit-spacing)")
val MarginRight2 = utilityOf("margin-right-2", "margin-right: var(--unit-spacing-2)")
val MarginRight4Px = utilityOf("margin-right-4px", "margin-right: 4px")
val MarginBottom1 = utilityOf("margin-bottom-1", "margin-bottom: var(--unit-spacing)")
val MarginBottom2 = utilityOf("margin-bottom-2", "margin-bottom: calc(var(--unit-spacing) * 2)")
val MarginBottom16 = utilityOf("margin-bottom-16", "margin-bottom: calc(var(--unit-spacing) * 16)")
val MarginX1 = utilityOf(
    "margin-x-1",
    "margin-left: var(--unit-spacing)",
    "margin-right: var(--unit-spacing)"
)
val MarginX2 = utilityOf(
    "margin-x-2",
    "margin-left: calc(var(--unit-spacing) * 2)",
    "margin-right: calc(var(--unit-spacing) * 2)"
)
val MarginX4 = utilityOf(
    "margin-x-4",
    "margin-left: calc(var(--unit-spacing) * 4)",
    "margin-right: calc(var(--unit-spacing) * 4)"
)

// Padding
val Padding0 = utilityOf("padding-0", "padding: 0")
val Padding1 = utilityOf("padding-1", "padding: var(--unit-spacing)")
val Padding2 = utilityOf("padding-2", "padding: calc(var(--unit-spacing) * 2)")
val Padding2Px = utilityOf("padding-2px", "padding: 2px")
val Padding4 = utilityOf("padding-4", "padding: calc(var(--unit-spacing) * 4)")
val PaddingTiny = utilityOf("padding-tiny", "padding: 2px")
val PaddingLeft1 = utilityOf("padding-left-1", "padding-left: var(--unit-spacing)")
val PaddingLeft2 = utilityOf("padding-left-2", "padding-left: var(--unit-spacing-2)")
val PaddingLeft3 = utilityOf("padding-left-3", "padding-left: var(--unit-spacing-3)")
val PaddingRight3 = utilityOf("padding-right-3", "padding-right: calc(var(--unit-spacing) * 3)")
val PaddingTop1 = utilityOf("padding-top-1", "padding-top: var(--unit-spacing)")
val PaddingX1 = utilityOf("padding-x-1", "padding-left: var(--unit-spacing)", "padding-right: var(--unit-spacing)")
val PaddingX2 = utilityOf("padding-x-2", "padding-left: var(--unit-spacing-2)", "padding-right: var(--unit-spacing-2)")
val PaddingY1 = utilityOf("padding-y-1", "padding-top: var(--unit-spacing)", "padding-bottom: var(--unit-spacing)")
val PaddingY2 = utilityOf("padding-y-2", "padding-top: var(--unit-spacing-2)", "padding-bottom: var(--unit-spacing-2)")
val PaddingBottom1 = utilityOf("padding-bottom-1", "padding-bottom: var(--unit-spacing)")

// Object Fit
val ObjectFitCover = utilityOf("object-fit-cover", "object-fit: cover")
val ObjectFitContain = utilityOf("object-fit-contain", "object-fit: contain")
val ObjectPositionCenter = utilityOf("object-position-center", "object-position: center")
val ObjectFitFill = utilityOf("object-fit-stretch", "object-fit: fill")

// Other
val SpaceBetween = utilityOf("space-between", "justify-content: space-between")
val Start = CssUtility("start")
val End = CssUtility("end")
val PlaceSelfCenter = utilityOf("place-self-center", "place-self: center")
val PlaceItemsCenter = utilityOf("place-items-center", "place-items: center")
val JustifySelfEnd = utilityOf("justify-self-end", "justify-self: end")
val JustifySelfCenter = utilityOf("justify-self-center", "justify-self: center")
val AlignSelfStart = utilityOf("align-self-start", "align-self: start")
val AlignSelfCenter = utilityOf("align-self-center", "align-self: center")
val AlignSelfEnd = utilityOf("align-self-end", "align-self: end")
val JustifySelfStart = utilityOf("justify-self-start", "justify-self: start")
val JustifySelfStretch = utilityOf("justify-self-stretch", "justify-self: stretch")
val AlignSelfStretch = utilityOf("align-self-stretch", "align-self: stretch")
val Aspect1 = utilityOf("aspect-1", "aspect-ratio: 1 / 1")
val Aspect2By1 = utilityOf("aspect-x-2", "aspect-ratio: 2 / 1")
val Aspect3By2 = utilityOf("aspect-x-3", "aspect-ratio: 3 / 2")
val Aspect3By1 = utilityOf("aspect-x-3", "aspect-ratio: 3 / 1")
val RelativeParent = CssUtility(
    "relative-parent",
    ".relative-parent { position: relative; } .relative-parent > * { position: absolute; inset: 0; }"
)
val ZIndex1 = utilityOf("z-index-1", "z-index: 1")
val ZIndex2 = utilityOf("z-index-2", "z-index: 2")
val ZIndex3 = utilityOf("z-index-3", "z-index: 3")
val Shrinkable = CssUtility("shrinkable")
val QueryContainer = utilityOf("query-container", "container-type: inline-size")
val PositionSticky = utilityOf("position-sticky", "position: sticky")
val PositionRelative = utilityOf("position-relative", "position: relative")
val PositionAbsolute = utilityOf("position-absolute", "position: absolute")
val Top0 = utilityOf("top-0", "top: 0")
val Left0 = utilityOf("left-0", "left: 0")
val Bottom0 = utilityOf("bottom-0", "bottom: 0")
val Right0 = utilityOf("right-0", "right: 0")
val TopSpacing1 = utilityOf("top-spacing-1", "top: var(--unit-spacing)")
val TopSpacing8 = utilityOf("top-spacing-8", "top: calc(var(--unit-spacing) * 8)")

// Query
val MediaMdRow = Class("media-md-row")
val MediaMdColumn = Class("media-md-column")
val MediaLgRow = Class("media-lg-row")
val MediaLgColumn = Class("media-lg-column")

// Container Query
val ContainerTnRow = Class("container-tn-row")
val ContainerSmRow = Class("container-sm-row")
val ContainerMdRow = Class("container-md-row")
val ContainerLgRow = Class("container-lg-row")

val ContainerTnColumn = Class("container-tn-column")
val ContainerSmColumn = Class("container-sm-column")
val ContainerMdColumn = Class("container-md-column")
val ContainerLgColumn = Class("container-lg-column")

val ContainerMdMarginTop0 = Class("container-md-margin-top-0")

// Layouts
val Box = Class("box")
val Row = Class("row")
val Column = Class("column")
val Card = Class("card")
