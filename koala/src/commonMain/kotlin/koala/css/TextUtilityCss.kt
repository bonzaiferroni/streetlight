package koala.css

val TextUtilityCss
    get() = listOf(
        // font weight/style
        Bold, Italic,
        // font size
        SmallText, LargeText, GrowText,
        // misc
        WhiteSpaceNormal, LineHeight1, LineHeight115, SingleLine, TextOverflowHidden,
    )

val Bold = CssUtility("bold", ".bold { font-weight: bold; }")
val Italic = CssUtility("italic", ".italic { font-style: italic; }")
val SmallText = CssUtility("small-text", ".small-text { font-size: 0.9em; }")
val LargeText = CssUtility("large-text", ".large-text { font-size: 1.2rem; }")
val GrowText = CssUtility("grow-text", ".grow-text { font-size: 1.1em; }")
val WhiteSpaceNormal = CssUtility("white-space-normal", ".white-space-normal { white-space: normal; }")
val LineHeight1 = CssUtility("line-height-1", ".line-height-1 { line-height: 1; }")
val LineHeight115 = CssUtility("line-height-1-15", ".line-height-1-15 { line-height: 1.15; }")
val SingleLine = CssUtility("single-line", ".single-line { white-space: nowrap; }")
val TextOverflowHidden = CssUtility("text-overflow-hidden", ".text-overflow-hidden { overflow: hidden; text-overflow: ellipsis; }")