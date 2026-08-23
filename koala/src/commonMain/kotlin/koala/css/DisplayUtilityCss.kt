package koala.css

val DisplayUtilityCss
    get() = listOf(
        // Display/Visibility
        DisplayNone, VisibilityHidden,
        // Opacity
        Opacity0, Opacity1, OpacityHigh, OpacityHalf, OpacityLow, OpacityGhost, Dim, NoDim,
        // Shape
        CircleShape, CircleClip,
        // Border Radius
        BorderRadius0, BorderRadius1, BorderRadius2, BorderRadius4, BorderRadius3, BorderRadius50P, BorderRadiusPill,
        BorderRadiusBottom1, BorderRadiusTop1, BorderDashed2Px, BorderSolid2Px, Outline, OutlineCurrentColor, OutlineEditorFg,
        OutlineDashed2Px, Chopped,
        // Border
        SideBorder,
        // Color
        NightInk, DayInk, PrimaryFg, AccentFg, ColorSchemeFg, ColorSchemeBg, EditorFg, WhiteFg, ErrorFg, CautionFg, ValidFg,
        // Overflow
        OverflowHidden, OverflowWrapAnywhere, OverflowClip, OverflowXAuto, OverflowXHidden, OverflowYAuto, OverflowYScroll,
        OverscrollBehaviorContain,
        // Background
        PrimaryBg, PrimaryCardBg, ZenBg, CardBg, VoidBg, EditorBg, DialogBg, BackgroundImage, SolidBg, BlurBackdrop, TransparentBg,
        PaperGradientBg, CardGradientBg, InkGradientBg,
        // Transform
        FlipX, FlipY,
        // Shadow
        MoonShadow, MoonShadowText, MoonShadowInset, MoonDropShadow,
        // Theme
        DayTheme,
        // Overlays
        VignetteOver, VignetteBehind, GradientDarkBottom,
        // Masks
        FadeBottom,
        // Button
        ButtonPadding, ButtonBorderRadius,
        // Misc
        Focus, Clickable, PointerEventsAuto, PointerEventsNone, BlurContent, AspectRatioAuto,
    )

// Display
// val DisplayNone = CssUtility("display-none", ".display-none { display: none !important; }")
val DisplayNone = utilityOf("display-none", "display: none !important")
val VisibilityHidden = utilityOf("visibility-hidden", "visibility: hidden !important")

// Opacity
// val Opacity1 = CssUtility("opacity-1", ".opacity-1 { opacity: 1; }")
val Opacity0 = utilityOf("opacity-0", "opacity: 0")
val Opacity1 = utilityOf("opacity-1", "opacity: 1")
val OpacityHigh = utilityOf("opacity-high", "opacity: var(--opacity-high)")
val OpacityHalf = utilityOf("opacity-half", "opacity: var(--opacity-half)")
val OpacityLow = utilityOf("opacity-low", "opacity: var(--opacity-low)")
val OpacityGhost = utilityOf("opacity-ghost", "opacity: .1")
val Dim = utilityOf("dim", "color: rgba(var(--ink), 0.6)")
val NoDim = utilityOf("no-dim", "color: rgb(var(--ink)) !important")

// Glow
val GlowShadow = Class("glow-shadow")
val AntiShadow = Class("anti-shadow")
val GlowBackground = Class("glow-background")
val SpinLoop = Class("spin-loop")
val FadeLoop = Class("fade-loop")

// Shape
val CircleShape = utilityOf("circle-shape", "border-radius: 50%", "overflow: hidden", "aspect-ratio: 1 / 1", "width: 100%")
val CircleClip = utilityOf("circle-clip", "border-radius: 50%", "overflow: hidden")

// Border Radius
val BorderRadius0 = utilityOf("border-radius-0", "border-radius: 0")
val BorderRadius1 = utilityOf("border-radius-1", "border-radius: var(--unit-spacing)")
val BorderRadius2 = utilityOf("border-radius-2", "border-radius: var(--unit-spacing-2)")
val BorderRadius3 = utilityOf("border-radius-3", "border-radius: var(--unit-spacing-3)")
val BorderRadius4 = utilityOf("border-radius-4", "border-radius: var(--unit-spacing-4)")
val BorderRadius50P = utilityOf("border-radius-50p", "border-radius: 50%")
val BorderRadiusPill = utilityOf("border-radius-x-50p", "border-radius: 9999px")
val BorderDashed2Px = utilityOf("border-dashed", "border: 2px dashed var(--outline-low-fg)")
val BorderSolid2Px = utilityOf("border-solid", "border: var(--outline-low)")
val BorderRadiusTop1 = utilityOf("border-radius-top-1", "border-radius: var(--unit-spacing) var(--unit-spacing) 0 0")
val BorderRadiusBottom1 = utilityOf("border-radius-bottom-1", "border-radius: 0 0 var(--unit-spacing) var(--unit-spacing)")
val Outline = utilityOf("outline-solid", "outline: var(--outline-low)", "outline-offset: -2px")
val OutlineCurrentColor = utilityOf("outline-current-color", "outline: 2px solid currentColor", "outline-offset: -2px")
val OutlineEditorFg = utilityOf("outline-editor-fg", "outline: 2px solid var(--editor-fg)", "outline-offset: -2px")
val OutlineDashed2Px = utilityOf("outline-dashed", "outline: 2px dashed var(--outline-low-fg)")
val Chopped = utilityOf("chopped", "--chop: var(--unit-spacing-8)",
    "clip-path: polygon(var(--chop) 0, 100% 0, 100% calc(100% - var(--chop)), calc(100% - var(--chop)) 100%, 0 100%, 0 var(--chop))")

// Border
val SideBorder = utilityOf("side-border", "border-left: var(--ghost-border)", "border-right: var(--ghost-border)")

// Color
val Accent = Class("accent")
val AccentFg = utilityOf("accent-fg", "color: var(--accent-fg)")
val Primary = Class("primary")
val Editor = Class("editor")
val PrimaryFg = utilityOf("primary-fg", "color: var(--primary-fg)")
val WhiteFg = utilityOf("white-fg", "color: var(--white-fg)")
val EditorFg = utilityOf("editor-fg", "color: var(--editor-fg)")
val ErrorFg = utilityOf("red-fg", "color: var(--error-fg)")
val CautionFg = utilityOf("caution-fg", "color: var(--caution-fg)")
val ValidFg = utilityOf("valid-fg", "color: var(--valid-fg)")
val ColorSchemeFg = utilityOf("color-scheme-fg", "color: var(--color-scheme, currentColor)")
val ColorSchemeBg = utilityOf("color-scheme-bg", "background-color: var(--color-scheme, currentColor)")
val PaperGradientBg = utilityOf("paper-gradient-bg", "background: var(--paper-gradient-bg)")
val CardGradientBg = utilityOf("card-gradient-bg", "background: var(--card-gradient-bg)")
val InkGradientBg = utilityOf("ink-gradient-bg", "background: var(--ink-gradient-bg)")
val Zen = Class("zen")
val Secondary = Class("secondary")
val Danger = Class("danger")
val Selected = Class("selected")
val Valid = Class("valid")
val Required = Class("required")
val Working = Class("working")
val NightInk = utilityOf("night-ink", "color: var(--white-fg)")
val DayInk = utilityOf("day-ink", "color: var(--black-fg)")

// Overflow
val OverflowHidden = utilityOf("overflow-hidden", "overflow: hidden")
val OverflowWrapAnywhere = utilityOf("overflow-wrap-anywhere", "overflow-wrap: anywhere")
val OverflowClip = utilityOf("overflow-clip", "overflow: clip")
val OverflowXAuto = utilityOf("overflow-x-auto", "overflow-x: auto")
val OverflowXHidden = utilityOf("overflow-x-hidden", "overflow-x: hidden")
val OverflowYAuto = utilityOf("overflow-y-auto", "overflow-y: auto")
val OverflowYScroll = utilityOf("overflow-y-scroll", "overflow-y: scroll")
val OverscrollBehaviorContain = utilityOf("overscroll-behavior-contain", "overscroll-behavior: contain")

// Background
val PrimaryBg = utilityOf("primary-bg", "background-color: var(--primary-bg)")
val PrimaryCardBg = utilityOf("primary-card-bg", "background-color: var(--primary-card-bg)")
val EditorBg = utilityOf("editor-bg", "background-color: var(--editor-bg)")
val ZenBg = utilityOf("zen-card-bg", "background: var(--zen-bg)")
val CardBg = utilityOf("card-bg", "background: var(--card-bg)")
val VoidBg = utilityOf("void-bg", "background: var(--void-bg)")
val DialogBg = utilityOf("dialog-bg", "background: var(--dialog-bg)")
val HeavyCardBg = utilityOf("heavy-card-bg", "background: rgba(var(--paper), .8)")
val BackgroundImage = CssUtility("background-image")
val SolidBg = utilityOf("solid-bg", "background-color: var(--paper-bg)")
val BlurBackdrop = utilityOf(
    "blur-backdrop",
    "backdrop-filter: var(--strong-blur)",
    "-webkit-backdrop-filter: var(--strong-blur)")
val TransparentBg = utilityOf("background-transparent", "background-color: transparent")

// Transform
val FlipX = utilityOf("flip-x", "transform: scaleX(-1)")
val FlipY = utilityOf("flip-y", "transform: scaleY(-1)")

// Shadow
val MoonShadow = utilityOf("moon-shadow", "box-shadow: var(--moon-shadow)")
val MoonShadowText = utilityOf("moon-shadow-text", "text-shadow: var(--moon-shadow-text)")
val MoonShadowInset = utilityOf("moon-shadow-inset", "box-shadow: var(--moon-shadow-inset)")
val MoonDropShadow = utilityOf("moon-drop-shadow", "filter: var(--moon-drop-shadow)")

// Button
val ButtonPadding = utilityOf("btn-padding", "padding: var(--btn-padding)")
val ButtonBorderRadius = utilityOf("btn-border-radius", "border-radius: var(--btn-border-radius)")

// Misc
val FadeBottom = utilityOf(
    "fade-bottom",
    "-webkit-mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%)",
    "mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%)")
val GradientDarkBottom = utilityOf("gradient-dark-bottom", """
background: linear-gradient(
    to bottom,
    transparent 50%,
    rgba(0, 0, 0, 0.7) 100%
);
""")

val VignetteOver = CssUtility("vignette-over", """
.vignette-over {
    position: relative;
}

.vignette-over::after {
    content: "";
    position: absolute;
    inset: 0;
    box-shadow: var(--vignette-shadow);
    pointer-events: none;
}
""")

val VignetteBehind = CssUtility("vignette-behind", """
.vignette-behind {
    position: relative;
}   
    
.vignette-behind::before {
    content: "";
    position: absolute;
    inset: 0;
    box-shadow: var(--vignette-shadow);
    pointer-events: none;
}
""")

val PointerEventsAuto = utilityOf("pointer-events-auto", "pointer-events: auto")
val PointerEventsNone = utilityOf("pointer-events-none", "pointer-events: none")
val BlurContent = utilityOf("blur-content", "filter: var(--strong-blur)")
val AspectRatioAuto = utilityOf("aspect-ratio-auto", "aspect-ratio: auto")


// defined in stylesheet
val DayTheme = CssUtility("day-theme")
val Activate = CssUtility("activate")
val Focus = CssUtility("focus")
val Clickable = CssUtility("clickable")