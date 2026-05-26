package koala.css

val DisplayUtilityCss
    get() = listOf(
        // Display/Visibility
        DisplayNone, VisibilityHidden,
        // Opacity
        Opacity1, OpacityMost, OpacityHalf, OpacitySome, OpacityGhost, Dim, NoDim,
        // Animation
        Glow, GlowShadow, GlowBackground, SpinLoop,
        // Shape
        CircleShape, CircleClip,
        // Border Radius
        BorderRadius0, BorderRadius1, BorderRadius2, BorderRadius4, BorderRadius3, BorderRadius50P, BorderRadiusBottom1,
        BorderDashed2Px,
        // Border
        SideBorder,
        // Color
        NightInk, DayInk, PrimaryFg, AccentFg, ColorSchemeFg, ColorSchemeBg,
        // Overflow
        OverflowHidden, OverflowWrapAnywhere, OverflowClip, OverflowXAuto, OverflowXHidden, OverflowYAuto, OverscrollBehaviorContain,
        // Background
        PrimaryBg, PrimaryCardBg, ZenBg, CardBg, BackgroundImage, SolidBg, BlurBackdrop, TransparentBg,
        // Transform
        FlipX, FlipY,
        // Shadow
        MoonShadow, MoonShadowText, MoonShadowInset,
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
val Opacity1 = utilityOf("opacity-1", "opacity: 1")
val OpacityMost = utilityOf("opacity-most", "opacity: .75")
val OpacityHalf = utilityOf("opacity-half", "opacity: .5")
val OpacitySome = utilityOf("opacity-some", "opacity: .25")
val OpacityGhost = utilityOf("opacity-ghost", "opacity: .1")
val Dim = utilityOf("dim", "color: rgba(var(--ink), 0.6)")
val NoDim = utilityOf("no-dim", "color: rgb(var(--ink)) !important")

// Glow
val Glow = CssUtility("glow")
val GlowShadow = CssUtility("glow-shadow")
val AntiShadow = CssUtility("anti-shadow")
val GlowBackground = CssUtility("glow-background")
val SpinLoop = CssUtility("spin-loop")
val FadeLoop = CssUtility("fade-loop")

// Shape
val CircleShape = utilityOf("circle-shape", "border-radius: 50%", "overflow: hidden", "border: 3px solid #b4bd7d")
val CircleClip = utilityOf("circle-clip", "border-radius: 50%", "overflow: hidden")

// Border Radius
val BorderRadius0 = utilityOf("border-radius-0", "border-radius: 0")
val BorderRadius1 = utilityOf("border-radius-1", "border-radius: var(--unit-spacing)")
val BorderRadius2 = utilityOf("border-radius-2", "border-radius: var(--unit-spacing-2)")
val BorderRadius3 = utilityOf("border-radius-3", "border-radius: var(--unit-spacing-3)")
val BorderRadius4 = utilityOf("border-radius-4", "border-radius: var(--unit-spacing-4)")
val BorderRadius50P = utilityOf("border-radius-50p", "border-radius: 50%")
val BorderDashed2Px = utilityOf("border-dashed", "border: 2px dashed currentColor")
val BorderRadiusBottom1 = utilityOf("border-radius-bottom-1", "border-radius: 0 0 var(--unit-spacing) var(--unit-spacing)")

// Border
val SideBorder = utilityOf("side-border", "border-left: var(--ghost-border)", "border-right: var(--ghost-border)")

// Color
val Accent = Class("accent")
val AccentFg = utilityOf("accent-fg", "color: var(--accent-fg)")
val Primary = Class("primary")
val PrimaryFg = utilityOf("primary-fg", "color: var(--primary-fg)")
val ColorSchemeFg = utilityOf("color-scheme-fg", "color: var(--color-scheme, currentColor)")
val ColorSchemeBg = utilityOf("color-scheme-bg", "background-color: var(--color-scheme, currentColor)")
val Zen = Class("zen")
val Secondary = Class("secondary")
val Danger = Class("danger")
val Confirm = Class("confirm")
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
val OverscrollBehaviorContain = utilityOf("overscroll-behavior-contain", "overscroll-behavior: contain")

// Background
val PrimaryBg = utilityOf("primary-bg", "background-color: var(--primary-bg)")
val PrimaryCardBg = utilityOf("primary-card-bg", "background-color: var(--primary-card-bg)")
val ZenBg = utilityOf("zen-card-bg", "background: var(--zen-bg)")
val CardBg = utilityOf("card-bg", "background: var(--card-bg)")
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