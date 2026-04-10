package koala.css

val DisplayUtilityCss
    get() = listOf(
        // Display
        DisplayNone,
        // Opacity
        Opacity1, OpacityMost, OpacityHalf, OpacitySome, Dim, NoDim,
        // Animation
        Glow, GlowShadow, GlowBackground, SpinLoop,
        // Shape
        CircleShape, CircleClip,
        // Border Radius
        BorderRadius0, BorderRadius1, BorderRadius2, BorderRadius4, BorderRadius50P,
        BorderDashed2Px,
        // Color
        Accent, Primary, Secondary, Danger,
        // Overflow
        OverflowHidden, OverflowWrapAnywhere, OverflowClip, OverflowXAuto,
        // Background
        PrimaryBg, PrimaryCardBg, ZenCardBg, CardBg, BackgroundImage, SolidBg, BlurBackdrop, TransparentBg,
        // Misc
        FadeBottom, Focus, Clickable, PointerEventsAuto, PointerEventsNone, BlurContent
    )

// Display
// val DisplayNone = CssUtility("display-none", ".display-none { display: none !important; }")
val DisplayNone = utilityOf("display-none", "display: none !important")

// Opacity
// val Opacity1 = CssUtility("opacity-1", ".opacity-1 { opacity: 1; }")
val Opacity1 = utilityOf("opacity-1", "opacity: 1")
val OpacityMost = utilityOf("opacity-most", "opacity: .75")
val OpacityHalf = utilityOf("opacity-half", "opacity: .5")
val OpacitySome = utilityOf("opacity-some", "opacity: .25")
val Dim = utilityOf("dim", "color: rgba(var(--ink), 0.6)")
val NoDim = utilityOf("no-dim", "color: rgb(var(--ink)) !important")

// Glow
val Glow = CssUtility("glow")
val GlowShadow = CssUtility("glow-shadow")
val GlowBackground = CssUtility("glow-background")
val SpinLoop = CssUtility("spin-loop")
val FadeLoop = CssUtility("fade-loop")

// Shape
val CircleShape = utilityOf("circle-shape", "border-radius: 50%", "overflow: hidden", "border: 3px solid #b4bd7d")
val CircleClip = utilityOf("circle-clip", "border-radius: 50%", "overflow: hidden")

// Border Radius
val BorderRadius0 = utilityOf("border-radius-0", "border-radius: 0")
val BorderRadius1 = utilityOf("border-radius-1", "border-radius: var(--unit-spacing)")
val BorderRadius2 = utilityOf("border-radius-2", "border-radius: calc(var(--unit-spacing) * 2)")
val BorderRadius4 = utilityOf("border-radius-4", "border-radius: calc(var(--unit-spacing) * 4)")
val BorderRadius50P = utilityOf("border-radius-50p", "border-radius: 50%")
val BorderDashed2Px = utilityOf("border: 2px dashed currentColor;")

// Color
val Accent = CssUtility("accent")
val Primary = CssUtility("primary")
val Secondary = CssUtility("secondary")
val Danger = CssUtility("danger")

// Overflow
val OverflowHidden = utilityOf("overflow-hidden", "overflow: hidden")
val OverflowWrapAnywhere = utilityOf("overflow-wrap-anywhere", "overflow-wrap: anywhere")
val OverflowClip = utilityOf("overflow-clip", "overflow: clip")
val OverflowXAuto = utilityOf("overflow-x-auto", "overflow-x: auto")

// Background
val PrimaryBg = utilityOf("primary-bg", "background-color: var(--primary-bg)")
val PrimaryCardBg = utilityOf("primary-card-bg", "background-color: var(--primary-card-bg)")
val ZenCardBg = utilityOf("zen-card-bg", "background: var(--zen-card-bg)")
val CardBg = utilityOf("card-bg", "background: var(--card-bg)")
val HeavyCardBg = utilityOf("heavy-card-bg", "background: rgba(var(--paper), .8)")
val BackgroundImage = CssUtility("background-image")
val SolidBg = utilityOf("solid-bg", "background-color: var(--paper-bg)")
val BlurBackdrop = utilityOf(
    "blur-backdrop",
    "backdrop-filter: var(--strong-blur)",
    "-webkit-backdrop-filter: var(--strong-blur)")
val TransparentBg = utilityOf("background-transparent", "background-color: transparent")

// Misc
val FadeBottom = utilityOf(
    "fade-bottom",
    "-webkit-mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%)",
    "mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%)")
val Focus = CssUtility("focus")
val Clickable = CssUtility("clickable")
val PointerEventsAuto = utilityOf("pointer-events-auto", "pointer-events: auto")
val PointerEventsNone = utilityOf("pointer-events-none", "pointer-events: none")
val BlurContent = utilityOf("blur-content", "filter: var(--strong-blur)")