package koala.modifier

// Opacity
val OpacityHigh = Class("opacity-high")
val OpacityHalf = Class("opacity-half")
val OpacityLow = Class("opacity-low")

// Glow
val GlowShadow = Class("glow-shadow")
val AntiShadow = Class("anti-shadow")
val GlowBackground = Class("glow-background")
val SpinLoop = Class("spin-loop")
val FadeLoop = Class("fade-loop")

// Shape
val CircleShape = Class("circle-shape")

// Border Radius
val BorderDashed2Px = Class("border-dashed")
val BorderSolid2Px = Class("border-solid")
val BorderRadiusTop1 = Class("border-radius-top-1")
val BorderRadiusBottom1 = Class("border-radius-bottom-1")
val Outline = Class("outline-solid")
val OutlineEditorFg = Class("outline-editor-fg")
val Chopped = Class("chopped")

// Border
val SideBorder = Class("side-border")

// Color
val Accent = Class("accent")
val AccentFg = Class("accent-fg")
val Primary = Class("primary")
val Editor = Class("editor")
val PrimaryFg = Class("primary-fg")
/** Text in a gradient from the accent to the primary foreground, left to right. */
val AccentPrimaryGradientFg = Class("accent-primary-gradient-fg")
val WhiteFg = Class("white-fg")
val SystemFg = Class("editor-fg")
val ErrorFg = Class("red-fg")
val CautionFg = Class("caution-fg")
val ValidFg = Class("valid-fg")
val InkDimFg = Class("dim")
val ColorSchemeFg = Class("color-scheme-fg")
val ColorSchemeBg = Class("color-scheme-bg")
val PaperGradientBg = Class("paper-gradient-bg")
val CardGradientBg = Class("card-gradient-bg")
val InkGradientBg = Class("ink-gradient-bg")
val Zen = Class("zen")
val Secondary = Class("secondary")
val Danger = Class("danger")
val Selected = Class("selected")
val Valid = Class("valid")
val Required = Class("required")
val NightInk = Class("night-ink")

// Overflow
val OverflowHidden = Class("overflow-hidden")
val OverflowWrapAnywhere = Class("overflow-wrap-anywhere")
val OverflowClip = Class("overflow-clip")
val OverflowXHidden = Class("overflow-x-hidden")
val OverflowYAuto = Class("overflow-y-auto")
val OverflowYScroll = Class("overflow-y-scroll")
val OverscrollBehaviorContain = Class("overscroll-behavior-contain")
val ScrollbarWidthNone = Class("scrollbar-width-none")

// Background
val PrimaryBg = Class("primary-bg")
val PrimaryCardBg = Class("primary-card-bg")
val SystemBg = Class("system-bg")
val InkBg = Class("ink-bg")
val ZenBg = Class("zen-card-bg")
val CardBg = Class("card-bg")
val VoidBg = Class("void-bg")
val HeavyCardBg = Class("heavy-card-bg")
val BackgroundImage = Class("background-image")
val HoverBg = Class("hover-bg")
val BlurBackdrop = Class("blur-backdrop")

// Transform
val FlipX = Class("flip-x")

// Shadow
val MoonShadow = Class("moon-shadow")
val MoonShadowText = Class("moon-shadow-text")
val MoonDropShadow = Class("moon-drop-shadow")

// Button
val ButtonPadding = Class("btn-padding")
val ButtonBorderRadius = Class("btn-border-radius")

// Misc
val FadeBottom = Class("fade-bottom")
val GradientDarkBottom = Class("gradient-dark-bottom")
val VignetteOver = Class("vignette-over")
val PointerEventsAuto = Class("pointer-events-auto")
val PointerEventsNone = Class("pointer-events-none")
val AspectAuto = Class("aspect-ratio-auto")

// defined in stylesheet
val DayTheme = Class("day-theme")
val Focus = Class("focus")
val Clickable = Class("clickable")

// language="CSS"
val DisplayCss get() = """
/* Opacity */
$OpacityHigh { opacity: var(--opacity-high); }
$OpacityHalf { opacity: var(--opacity-half); }
$OpacityLow  { opacity: var(--opacity-low); }

/* Shape */
$CircleShape {
    border-radius: 50%;
    overflow: hidden;
    aspect-ratio: 1 / 1;
    width: 100%;
}

/* Border Radius */
$BorderDashed2Px     { border:        2px dashed var(--outline-low-fg); }
$BorderSolid2Px      { border:        var(--outline-low); }
$BorderRadiusTop1    { border-radius: var(--unit) var(--unit) 0 0; }
$BorderRadiusBottom1 { border-radius: 0 0 var(--unit) var(--unit); }
$Outline             { outline:       var(--outline-low); outline-offset: -2px; }
$OutlineEditorFg     { outline:       2px solid var(--system-fg); outline-offset: -2px; }

$Chopped {
    --chop: var(--unit-8);
    clip-path: polygon(var(--chop) 0, 100% 0, 100% calc(100% - var(--chop)), calc(100% - var(--chop)) 100%, 0 100%, 0 var(--chop));
}

/* Border */
$SideBorder { border-left: var(--ghost-border); border-right: var(--ghost-border); }

/* Color */
$AccentFg        { color:            var(--accent-fg); }
$PrimaryFg       { color:            var(--primary-fg); }
$WhiteFg         { color:            var(--white-fg); }
$SystemFg        { color:            var(--system-fg); }
$ErrorFg         { color:            var(--error-fg); }
$CautionFg       { color:            var(--caution-fg); }
$ValidFg         { color:            var(--valid-fg); }
$InkDimFg        { color:            rgba(var(--ink), 0.6); }
$ColorSchemeFg   { color:            var(--color-scheme, currentColor); }
$ColorSchemeBg   { background-color: var(--color-scheme, currentColor); }
$PaperGradientBg { background:       var(--paper-gradient-bg); }
$CardGradientBg  { background:       var(--card-gradient-bg); }

$InkGradientBg {
    background: color-mix(in srgb, currentColor 50%, transparent);
    mask-image: var(--ink-gradient-bg);
}

$AccentPrimaryGradientFg {
    background-image: linear-gradient(to right, var(--accent-fg), var(--primary-fg));
    background-clip: text;
    color: transparent;
}

$NightInk { color: var(--white-fg); }

/* Overflow */
$OverflowHidden            { overflow:            hidden; }
$OverflowWrapAnywhere      { overflow-wrap:       anywhere; }
$OverflowClip              { overflow:            clip; }
$OverflowXHidden           { overflow-x:          hidden; }
$OverflowYAuto             { overflow-y:          auto; }
$OverflowYScroll           { overflow-y:          scroll; }
$OverscrollBehaviorContain { overscroll-behavior: contain; }
$ScrollbarWidthNone        { scrollbar-width:     none; }

/* Background */
$PrimaryBg     { background-color: var(--primary-bg); }
$PrimaryCardBg { background-color: var(--primary-card-bg); }
$SystemBg      { background-color: var(--system-bg); }
$InkBg         { background-color: var(--ink-fg); }
$ZenBg         { background:       var(--zen-bg); }
$CardBg        { background:       var(--card-bg); }
$VoidBg        { background:       var(--void-bg); }
$HeavyCardBg   { background:       rgba(var(--paper), .8); }

$BlurBackdrop {
    backdrop-filter: var(--strong-blur);
    -webkit-backdrop-filter: var(--strong-blur);
}

/* Transform */
$FlipX { transform: scaleX(-1); }

/* Shadow */
$MoonShadow     { box-shadow:  var(--moon-shadow); }
$MoonShadowText { text-shadow: var(--moon-shadow-text); }
$MoonDropShadow { filter:      var(--moon-drop-shadow); }

/* Button */
$ButtonPadding      { padding:       var(--btn-padding); }
$ButtonBorderRadius { border-radius: var(--btn-border-radius); }

/* Misc */
$FadeBottom {
    -webkit-mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%);
    mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%);
}

$GradientDarkBottom {
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.7) 100%);
}

$VignetteOver { position: relative; }

$VignetteOver::after {
    content: "";
    position: absolute;
    inset: 0;
    box-shadow: var(--vignette-shadow);
    pointer-events: none;
}

$PointerEventsAuto { pointer-events: auto; }
$PointerEventsNone { pointer-events: none; }
$AspectAuto        { aspect-ratio:   auto; }
"""
