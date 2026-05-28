package koala.css

val TextUtilityCss
    get() = listOf(
        // font weight/style
        Bold, Italic,
        // font size
        SmallText, LargeText, GrowText,
        // line height
        LineHeight1, LineHeight115, ParagraphLineHeight,
        // misc
        SingleLine, WhiteSpaceNormal, TextOverflowEllipses, TextShadow, WhiteSpaceNoWrap, UserSelectNone, Prose,
    )

val Bold = utilityOf("bold", "font-weight: bold")
val Italic = utilityOf("italic", "font-style: italic")
val SmallText = utilityOf("small-text", "font-size: .9rem")
val LargeText = utilityOf("large-text", "font-size: 1.2rem")
val GrowText = utilityOf("grow-text", "font-size: 1.1em")
val WhiteSpaceNormal = utilityOf("white-space-normal", "white-space: normal")
val LineHeight1 = utilityOf("line-height-1", "line-height: 1")
val LineHeight115 = utilityOf("line-height-1-15", "line-height: 1.15")
val ParagraphLineHeight = utilityOf("paragraph-line-height", "line-height: var(--paragraph-line-height)")
val WhiteSpaceNoWrap = utilityOf("white-space-no-wrap", "white-space: nowrap")
val SingleLine = utilityOf("single-line", "white-space: nowrap", "overflow: hidden", "text-overflow: ellipsis")
val TextOverflowEllipses = utilityOf("text-overflow-ellipsis", "overflow: hidden", "text-overflow: ellipsis")
val TextShadow = utilityOf("text-shadow", "text-shadow: var(--ink-shadow)")
val UserSelectNone = utilityOf("user-select-none", "user-select: none")

// defined in stylesheet
val Prose = CssUtility("prose")
val ButtonText = CssUtility("btn-text")