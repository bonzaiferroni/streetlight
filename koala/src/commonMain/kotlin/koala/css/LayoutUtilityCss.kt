package koala.css

val LayoutUtilityCss
    get() = listOf(
        // Gap
        Gap0, Gap1, Gap2, Gap4, Gap8, GapTiny,
        // Flex
        Flex0, Flex1, Flex2, Flex3, Flex4, FlexBasis25, FlexBasis33, FlexShrink0,
        RowReverse, QueryRowReverse, NoWrap, WrapFlex, DisplayFlex,
        // Align
        AlignItemsCenter, AlignItemsStretch, AlignItemsStart, AlignItemsEnd,
        JustifyContentCenter, JustifyContentEnd, JustifyContentSpaceAround, JustifyContentSpaceBetween,
        JustifySelfEnd, AlignSelfStart, AlignSelfStretch,
        // Width
        WidthAuto, Width100P, WidthFitContent,
        Width2, Width4, Width5, Width8, Width12, Width16, Width24, Width32, Width64,
        MinWidth0, MinWidth16, MinWidth32, MinWidthAuto, MaxWidth25P, MaxWidth50P,
        MaxWidth5, MaxWidth16, MaxWidth64,
        // Height
        HeightAuto, Height100P, Height2, Height3, Height4, Height5, Height6, Height8, Height12, Height16, Height24, Height32, Height48,
        MinHeightAuto, MinHeight0, MinHeight4, MinHeight5, MinHeight8, MinHeight16, MinHeight24, MinHeight32,
        MaxHeight5, MaxHeight8, MaxHeight16, MaxHeight24, MaxHeight32, MaxHeight64,
        // Size
        Size100P, FillHeight, AspectRatio1, AspectRatio2,
        // Flex Items
        FlexItems1, FlexItemsBasis50,
        // Text
        TextAlignCenter, TextAlignRight,
        // Margin
        MarginAuto, Margin1, Margin2, MarginTop1, MarginTop2, MarginTop4, MarginTop8, MarginLeft1, MarginBottom16,
        MarginX1, MarginX2, MarginX4,
        // Padding
        Padding0, Padding1, Padding2, PaddingTiny, PaddingLeft3,
        // Object Fit
        ObjectFitCover, ObjectFitContain, ObjectPositionCenter,
        // Other
        SpaceBetween, Start, End, Center, PlaceItemsCenter, RelativeParent, ZIndex1, Shrinkable, QueryContainer
    )

// Gap
val Gap0 = utilityOf("gap-0", "gap: 0")
val Gap1 = utilityOf("gap-1", "gap: var(--unit-spacing)")
val Gap2 = utilityOf("gap-2", "gap: calc(var(--unit-spacing) * 2)")
val Gap4 = utilityOf("gap-4", "gap: calc(var(--unit-spacing) * 4)")
val Gap8 = utilityOf("gap-8", "gap: calc(var(--unit-spacing) * 8)")
val GapTiny = utilityOf("gap-tiny", "gap: 2px")

// Flex
val Flex0 = utilityOf("flex-0", "flex: 0")
val Flex1 = utilityOf("flex-1", "flex: 1")
val Flex2 = utilityOf("flex-2", "flex: 2")
val Flex3 = utilityOf("flex-3", "flex: 3")
val Flex4 = utilityOf("flex-4", "flex: 4")
val FlexMd1 = CssUtility("flex-md-1")
val FlexMd2 = CssUtility("flex-md-2")
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

// Width
val WidthAuto = utilityOf("width-auto", "width: auto")
val Width100P = utilityOf("width-100", "width: 100%")
val WidthFitContent = utilityOf("width-fit-content", "width: fit-content")
val Width2 = utilityOf("width-2", "width: calc(var(--unit-spacing) * 2)")
val Width4 = utilityOf("width-4", "width: calc(var(--unit-spacing) * 4)")
val Width5 = utilityOf("width-5", "width: calc(var(--unit-spacing) * 5)")
val Width8 = utilityOf("width-8", "width: calc(var(--unit-spacing) * 8)")
val Width12 = utilityOf("width-12", "width: calc(var(--unit-spacing) * 12)")
val Width16 = utilityOf("width-16", "width: calc(var(--unit-spacing) * 16)")
val Width24 = utilityOf("width-24", "width: calc(var(--unit-spacing) * 24)")
val Width32 = utilityOf("width-32", "width: calc(var(--unit-spacing) * 32)")
val Width64 = utilityOf("width-64", "width: calc(var(--unit-spacing) * 64)")
val MinWidth0 = utilityOf("min-width-0", "min-width: 0")
val MinWidth16 = utilityOf("min-width-16", "min-width: calc(var(--unit-spacing) * 16)")
val MinWidth32 = utilityOf("min-width-32", "min-width: calc(var(--unit-spacing) * 32)")
val MinWidthAuto = utilityOf("min-width-auto", "min-width: auto")
val MaxWidth25P = utilityOf("max-width-25", "max-width: 25%")
val MaxWidth50P = utilityOf("max-width-50", "max-width: 50%")
val MaxWidth5 = utilityOf("max-width-5", "max-width: calc(var(--unit-spacing) * 5)")
val MaxWidth16 = utilityOf("max-width-16", "max-width: calc(var(--unit-spacing) * 16)")
val MaxWidth64 = utilityOf("max-width-64", "max-width: calc(var(--unit-spacing) * 64)")

// Height
val Height100P = utilityOf("height-100", "height: 100%")
val HeightAuto = utilityOf("height-auto", "height: auto")
val Height2 = utilityOf("height-2", "height: calc(var(--unit-spacing) * 2)")
val Height3 = utilityOf("height-3", "height: calc(var(--unit-spacing) * 3)")
val Height4 = utilityOf("height-4", "height: calc(var(--unit-spacing) * 4)")
val Height5 = utilityOf("height-5", "height: calc(var(--unit-spacing) * 5)")
val Height6 = utilityOf("height-6", "height: calc(var(--unit-spacing) * 6)")
val Height8 = utilityOf("height-8", "height: calc(var(--unit-spacing) * 8)")
val Height12 = utilityOf("height-12", "height: calc(var(--unit-spacing) * 12)")
val Height16 = utilityOf("height-16", "height: calc(var(--unit-spacing) * 16)")
val Height24 = utilityOf("height-24", "height: calc(var(--unit-spacing) * 24)")
val Height32 = utilityOf("height-32", "height: calc(var(--unit-spacing) * 32)")
val Height48 = utilityOf("height-48", "height: calc(var(--unit-spacing) * 48)")
val MaxHeight5 = utilityOf("max-height-5", "max-height: calc(var(--unit-spacing) * 5)")
val MaxHeight8 = utilityOf("max-height-8", "max-height: calc(var(--unit-spacing) * 8)")
val MaxHeight16 = utilityOf("max-height-16", "max-height: calc(var(--unit-spacing) * 16)")
val MaxHeight24 = utilityOf("max-height-24", "max-height: calc(var(--unit-spacing) * 24)")
val MaxHeight32 = utilityOf("max-height-32", "max-height: calc(var(--unit-spacing) * 32)")
val MaxHeight64 = utilityOf("max-height-64", "max-height: calc(var(--unit-spacing) * 64)")
val MinHeightAuto = utilityOf("min-height-auto", "min-height: auto")
val MinHeight0 = utilityOf("min-height-0", "min-height: 0")
val MinHeight4 = utilityOf("min-height-4", "min-height: calc(var(--unit-spacing) * 4)")
val MinHeight5 = utilityOf("min-height-5", "min-height: calc(var(--unit-spacing) * 5)")
val MinHeight8 = utilityOf("min-height-8", "min-height: calc(var(--unit-spacing) * 8)")
val MinHeight16 = utilityOf("min-height-16", "min-height: calc(var(--unit-spacing) * 16)")
val MinHeight24 = utilityOf("min-height-24", "min-height: calc(var(--unit-spacing) * 24)")
val MinHeight32 = utilityOf("min-height-32", "min-height: calc(var(--unit-spacing) * 32)")

// Size
val Size100P = utilityOf("size-100", "width: 100%", "height: 100%")
val FillHeight = utilityOf("fill-height", "height: auto", "max-height: 100%")

// Flex Items
val FlexItems1 = utilityOf("flex-items-1 > *", "flex: 1")
val FlexItemsBasis50 = utilityOf("flex-items-basis-50 > *", "flex-basis: 50%")

// Text
val TextAlignCenter = utilityOf("text-align-center", "text-align: center")
val TextAlignRight = utilityOf("text-align-right", "text-align: right")

// Row / Wrap
val RowReverse = utilityOf("row-reverse", "flex-direction: row-reverse")
val QueryRowReverse = CssUtility("query-row-reverse")
val NoWrap = utilityOf("no-wrap", "flex-wrap: nowrap", "white-space: nowrap")
val WrapFlex = utilityOf("wrap-flex", "flex-wrap: wrap")
val DisplayFlex = utilityOf("display-flex", "display: flex")

// Margin
val MarginAuto = utilityOf("margin-auto", "margin: auto")
val Margin1 = utilityOf("margin-1", "margin: var(--unit-spacing)")
val Margin2 = utilityOf("margin-2", "margin: calc(var(--unit-spacing) * 2)")
val MarginTop1 = utilityOf("margin-top-1", "margin-top: var(--unit-spacing)")
val MarginTop2 = utilityOf("margin-top-2", "margin-top: calc(var(--unit-spacing) * 2)")
val MarginTop4 = utilityOf("margin-top-4", "margin-top: calc(var(--unit-spacing) * 4)")
val MarginTop8 = utilityOf("margin-top-8", "margin-top: calc(var(--unit-spacing) * 8)")
val MarginLeft1 = utilityOf("margin-left-1", "margin-left: var(--unit-spacing)")
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
val PaddingTiny = utilityOf("padding-tiny", "padding: 2px")
val PaddingLeft3 = utilityOf("padding-left-3", "padding-left: calc(var(--unit-spacing) * 3)")

// Object Fit
val ObjectFitCover = utilityOf("object-fit-cover", "object-fit: cover")
val ObjectFitContain = utilityOf("object-fit-contain", "object-fit: contain")
val ObjectPositionCenter = utilityOf("object-position-center", "object-position: center")

// Other
val SpaceBetween = utilityOf("space-between", "justify-content: space-between")
val Start = CssUtility("start")
val End = CssUtility("end")
val Center = CssUtility("center")
val PlaceItemsCenter = CssUtility("center-items")
val JustifySelfEnd = utilityOf("justify-self-end", "justify-self: end")
val AlignSelfStart = utilityOf("align-self-start", "align-self: start")
val AlignSelfStretch = utilityOf("align-self-stretch", "align-self: stretch")
val AspectRatio1 = utilityOf("square", "aspect-ratio: 1 / 1")
val AspectRatio2 = utilityOf("aspect-ratio-2", "aspect-ratio: 2 / 1")
val RelativeParent = CssUtility(
    "relative-parent",
    ".relative-parent { position: relative; } .relative-parent > * { position: absolute; inset: 0; }"
)
val ZIndex1 = utilityOf("z-index-1", "z-index: 1")
val Shrinkable = CssUtility("shrinkable")
val QueryContainer = utilityOf("query-container", "container-type: inline-size")

// Query
val MediaMdRow = CssUtility("media-md-row")
val MediaMdColumn = CssUtility("media-md-column")
val MediaLgRow = CssUtility("media-lg-row")
val MediaLgColumn = CssUtility("media-lg-column")

// Container Query
val ContainerTnRow = CssUtility("container-tn-row")
val ContainerSmRow = CssUtility("container-sm-row")
val ContainerMdRow = CssUtility("container-md-row")
val ContainerLgRow = CssUtility("container-lg-row")

val ContainerTnColumn = CssUtility("container-tn-column")
val ContainerSmColumn = CssUtility("container-sm-column")
val ContainerMdColumn = CssUtility("container-md-column")
val ContainerLgColumn = CssUtility("container-lg-column")

val ContainerMdMarginTop0 = CssUtility("container-md-margin-top-0")

// Layouts
val Box = CssUtility("box")
val Row = CssUtility("row")
val Column = CssUtility("column")
val Card = CssUtility("card")
