package koala.modifier

// Font
val Bold = Class("bold")
val Italic = Class("italic")
val TextSmall = Class("text-small")
val TextLarge = Class("text-large")

// Line Height
val LineHeight1 = Class("line-height-1")
val LineHeight115 = Class("line-height-1-15")
val ParagraphLineHeight = Class("paragraph-line-height")

// Misc
val SingleLine = Class("single-line")
val TextOverflowEllipses = Class("text-overflow-ellipsis")
val TextShadow = Class("text-shadow")
val WhiteSpaceNoWrap = Class("white-space-no-wrap")
val WhiteSpacePreLine = Class("white-space-pre-line")
val UserSelectNone = Class("user-select-none")
val TextUppercase = Class("text-transform-uppercase")

// defined in stylesheet
val ButtonText = Class("btn-text")

// accessibility
val FocusTarget = Class("focus-target")

val LineClamp2 = Class("line-clamp-2")

// language="CSS"
val TextCss get() = """
/* Font */
$Bold      { font-weight: 600; }
$Italic    { font-style:  italic; }
$TextSmall { font-size:   var(--text-small) !important; }
$TextLarge { font-size:   var(--text-large) !important; }

/* Line Height */
$LineHeight1         { line-height: 1; }
$LineHeight115       { line-height: 1.15; }
$ParagraphLineHeight { line-height: var(--paragraph-line-height); }

/* Misc */
$SingleLine {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

$TextOverflowEllipses { overflow:       hidden; text-overflow: ellipsis; }
$TextShadow           { text-shadow:    var(--text-shadow); }
$WhiteSpaceNoWrap     { white-space:    nowrap; }
$WhiteSpacePreLine    { white-space:    pre-line; }
$UserSelectNone       { user-select:    none; }
$TextUppercase        { text-transform: uppercase; }

$LineClamp2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}
"""
