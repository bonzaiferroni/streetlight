package koala.modifier

val LayoutUtilityCss
    get() = listOf(
        // Flex
        NoWrap,
        // Size
        Size100P, FillHeight,
        // Flex Items
        FlexItems1, FlexItemsEqual, FlexItemsBasis50,
        // Margin
        MarginX1, MarginX2, MarginX4,
        // Padding
        PaddingX1, PaddingX2, PaddingY1, PaddingY2,
        // Other
        RelativeParent,
    )

// Size
val Size100P = utilityOf("size-100", "width: 100%", "height: 100%")
val FillHeight = utilityOf("fill-height", "height: auto", "max-height: 100%")

// Flex Items
val FlexItems1 = UtilityClass("flex-items-1", ".flex-items-1 > * { flex: 1; }")
val FlexItemsEqual = UtilityClass("flex-items-equal", ".flex-items-equal > * { flex: 1 1 0; min-width: 0; }")
val FlexItemsBasis50 = UtilityClass("flex-items-basis-50", ".flex-items-basis-50 > * { flex-basis: 50%; }")

// Row / Wrap
val NoWrap = utilityOf("no-wrap", "flex-wrap: nowrap", "white-space: nowrap")

// Margin
val MarginX1 = utilityOf(
    "margin-x-1",
    "margin-left: var(--unit)",
    "margin-right: var(--unit)"
)
val MarginX2 = utilityOf(
    "margin-x-2",
    "margin-left: calc(var(--unit) * 2)",
    "margin-right: calc(var(--unit) * 2)"
)
val MarginX4 = utilityOf(
    "margin-x-4",
    "margin-left: calc(var(--unit) * 4)",
    "margin-right: calc(var(--unit) * 4)"
)

// Padding
val PaddingX1 = utilityOf("padding-x-1", "padding-left: var(--unit)", "padding-right: var(--unit)")
val PaddingX2 = utilityOf("padding-x-2", "padding-left: var(--unit-2)", "padding-right: var(--unit-2)")
val PaddingY1 = utilityOf("padding-y-1", "padding-top: var(--unit)", "padding-bottom: var(--unit)")
val PaddingY2 = utilityOf("padding-y-2", "padding-top: var(--unit-2)", "padding-bottom: var(--unit-2)")

// Other
val RelativeParent = UtilityClass(
    "relative-parent",
    ".relative-parent { position: relative; } .relative-parent > * { position: absolute; inset: 0; }"
)
val Shrinkable = Class("shrinkable")

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

