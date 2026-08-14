package koala.css

val TextUtilityCss
    get() = listOf(
        // font weight/style
        Bold, Italic, TextSmall, TextLarge, GrowText, TextThin,
        // line height
        LineHeight1, LineHeight115, ParagraphLineHeight,
        // misc
        SingleLine, WhiteSpaceNormal, TextOverflowEllipses, TextShadow, WhiteSpaceNoWrap, WhiteSpacePreLine, UserSelectNone,
        TextTransformUppercase,
    )

val Bold = utilityOf("bold", "font-weight: 600")
val Italic = utilityOf("italic", "font-style: italic")
val TextSmall = utilityOf("text-small", "font-size: var(--text-small) !important")
val TextLarge = utilityOf("text-large", "font-size: var(--text-large) !important")
val TextThin = utilityOf("text-thin", "font-weight: 300")
val GrowText = utilityOf("grow-text", "font-size: 1.1em")
val WhiteSpaceNormal = utilityOf("white-space-normal", "white-space: normal")
val LineHeight1 = utilityOf("line-height-1", "line-height: 1")
val LineHeight115 = utilityOf("line-height-1-15", "line-height: 1.15")
val ParagraphLineHeight = utilityOf("paragraph-line-height", "line-height: var(--paragraph-line-height)")
val WhiteSpaceNoWrap = utilityOf("white-space-no-wrap", "white-space: nowrap")
val WhiteSpacePreLine = utilityOf("white-space-pre-line", "white-space: pre-line")
val SingleLine = utilityOf("single-line", "white-space: nowrap", "overflow: hidden", "text-overflow: ellipsis")
val TextOverflowEllipses = utilityOf("text-overflow-ellipsis", "overflow: hidden", "text-overflow: ellipsis")
val TextShadow = utilityOf("text-shadow", "text-shadow: var(--btn-text-shadow)")
val UserSelectNone = utilityOf("user-select-none", "user-select: none")
val TextTransformUppercase = utilityOf("text-transform-uppercase", "text-transform: uppercase")

// defined in stylesheet
val Prose = Class("prose")
val ButtonText = Class("btn-text")
val LineClamp2 = Class("line-clamp-2")

// accessibility
val FocusTarget = Class("focus-target")

//language="CSS"
val TextUtilitySheet get() = """
$LineClamp2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}
""".trimIndent()