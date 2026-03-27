package koala.css

val FontUtilityCss
    get() = listOf(
        // font weight/style
        FontWeightBold, Italic,
        // font size
        SmallFont, LargeFont,
        // misc
        WhiteSpaceNormal, LineHeight1
    )

val FontWeightBold = CssUtility("bold", ".bold { font-weight: bold; }")
val Italic = CssUtility("italic", ".italic { font-style: italic; }")
val SmallFont = CssUtility("small-font", ".small-font { font-size: 0.8rem; }")
val LargeFont = CssUtility("large-font", ".large-font { font-size: 1.4rem; }")
val WhiteSpaceNormal = CssUtility("white-space-normal", ".white-space-normal { white-space: normal; }")
val LineHeight1 = CssUtility("line-height-1", ".line-height-1 { line-height: 1; }")