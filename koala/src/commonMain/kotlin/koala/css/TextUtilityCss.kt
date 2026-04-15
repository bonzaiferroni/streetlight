package koala.css

val TextUtilityCss
    get() = listOf(
        // font weight/style
        Bold, Italic,
        // font size
        SmallText, LargeText, GrowText,
        // line height
        LineHeight1, LineHeight115,
        // misc
        WhiteSpaceNormal, TextOverflowHidden, TextShadow, WhiteSpaceNoWrap, Prose,
    )

val Bold = utilityOf("bold", "font-weight: bold")
val Italic = utilityOf("italic", "font-style: italic")
val SmallText = utilityOf("small-text", "font-size: 0.9em")
val LargeText = utilityOf("large-text", "font-size: 1.2rem")
val GrowText = utilityOf("grow-text", "font-size: 1.1em")
val WhiteSpaceNormal = utilityOf("white-space-normal", "white-space: normal")
val LineHeight1 = utilityOf("line-height-1", "line-height: 1")
val LineHeight115 = utilityOf("line-height-1-15", "line-height: 1.15")
val WhiteSpaceNoWrap = utilityOf("white-space-no-wrap", "white-space: nowrap")
val TextOverflowHidden = utilityOf("text-overflow-hidden", "overflow: hidden", "text-overflow: ellipsis")
val TextShadow = utilityOf("text-shadow", "text-shadow: var(--ink-shadow)")
val Prose = CssUtility("prose")