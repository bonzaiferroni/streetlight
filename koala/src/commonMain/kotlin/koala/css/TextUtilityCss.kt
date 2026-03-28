package koala.css

val TextUtilityCss
    get() = listOf(
        // font weight/style
        Bold, Italic,
        // font size
        SmallText, LargeText,
        // misc
        WhiteSpaceNormal, LineHeight1, LineHeight115, SingleLine, TextOverflowHidden,
    )

val Bold = CssUtility("bold", ".bold { font-weight: bold; }")
val Italic = CssUtility("italic", ".italic { font-style: italic; }")
val SmallText = CssUtility("small-text", ".small-text { font-size: 0.8rem; }")
val LargeText = CssUtility("large-text", ".large-text { font-size: 1.4rem; }")
val WhiteSpaceNormal = CssUtility("white-space-normal", ".white-space-normal { white-space: normal; }")
val LineHeight1 = CssUtility("line-height-1", ".line-height-1 { line-height: 1; }")
val LineHeight115 = CssUtility("line-height-1-15", ".line-height-1-15 { line-height: 1.15; }")
val SingleLine = CssUtility("single-line", ".single-line { white-space: nowrap; }")
val TextOverflowHidden = CssUtility("text-overflow-hidden", ".text-overflow-hidden { overflow: hidden; text-overflow: ellipsis; }")