package koala.css

val DisplayUtilityCss
    get() = listOf(
        // Display
        DisplayNone,
        // Opacity
        Opacity1, OpacityMost, OpacityHalf, OpacitySome, Dim, NoDim,
        // Glow
        Glow, GlowShadow, GlowBackground,
        // Shape
        CircleShape, CircleClip,
        // Border Radius
        BorderRadius0, BorderRadius1, BorderRadius2, BorderRadius4,
        // Color
        Accent, Primary, Secondary, Danger,
        // Overflow
        OverflowHidden, OverflowWrapAnywhere, OverflowClip, OverflowXAuto,
        // Background
        PrimaryBg, PrimaryCardBg, BackgroundImage, SolidBg, BlurBackdrop,
        // Misc
        FadeBottom, Focus, Clickable, PointerEventsAuto, PointerEventsNone,
    )

// Display
val DisplayNone = CssUtility("display-none", ".display-none { display: none !important; }")

// Opacity
val Opacity1 = CssUtility("opacity-1", ".opacity-1 { opacity: 1; }")
val OpacityMost = CssUtility("opacity-most", ".opacity-most { opacity: .75; }")
val OpacityHalf = CssUtility("opacity-half", ".opacity-half { opacity: .5; }")
val OpacitySome = CssUtility("opacity-some", ".opacity-some { opacity: .25; }")
val Dim = CssUtility("dim", ".dim { color: rgba(var(--ink), 0.6); }")
val NoDim = CssUtility("no-dim", ".no-dim { color: rgb(var(--ink)) !important; }")

// Glow
val Glow = CssUtility("glow", null)
val GlowShadow = CssUtility("glow-shadow", null)
val GlowBackground = CssUtility("glow-background", null)

// Shape
val CircleShape = CssUtility("circle-shape", ".circle-shape { border-radius: 50%; overflow: hidden; border: 3px solid #b4bd7d; }")
val CircleClip = CssUtility("circle-clip", ".circle-clip { border-radius: 50%; overflow: hidden; }")

// Border Radius
val BorderRadius0 = CssUtility("border-radius-0", ".border-radius-0 { border-radius: 0; }")
val BorderRadius1 = CssUtility("border-radius-1", ".border-radius-1 { border-radius: var(--unit-spacing); }")
val BorderRadius2 = CssUtility("border-radius-2", ".border-radius-2 { border-radius: calc(var(--unit-spacing) * 2); }")
val BorderRadius4 = CssUtility("border-radius-4", ".border-radius-4 { border-radius: calc(var(--unit-spacing) * 4); }")

// Color
val Accent = CssUtility("accent", null)
val Primary = CssUtility("primary", null)
val Secondary = CssUtility("secondary", null)
val Danger = CssUtility("danger", null)

// Overflow
val OverflowHidden = CssUtility("overflow-hidden", ".overflow-hidden { overflow: hidden; }")
val OverflowWrapAnywhere = CssUtility("overflow-wrap-anywhere", ".overflow-wrap-anywhere { overflow-wrap: anywhere; }")
val OverflowClip = CssUtility("overflow-clip", ".overflow-clip { overflow: clip; }")
val OverflowXAuto = CssUtility("overflow-x-auto", ".overflow-x-auto { overflow-x: auto; }")

// Background
val PrimaryBg = CssUtility("primary-bg", ".primary-bg { background-color: var(--primary-bg); }")
val PrimaryCardBg = CssUtility("primary-card-bg", ".primary-card-bg { background-color: var(--primary-card-bg); }")
val BackgroundImage = CssUtility("background-image", null)
val SolidBg = CssUtility("solid-bg", ".solid-bg { background-color: var(--paper-bg); }")
val BlurBackdrop = CssUtility("blur-backdrop", ".blur-backdrop { backdrop-filter: var(--strong-blur); -webkit-backdrop-filter: var(--strong-blur);  }")

// Misc
val FadeBottom = CssUtility("fade-bottom", ".fade-bottom { -webkit-mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%); mask-image: linear-gradient(to bottom, black 0, black calc(100% - 1rem), transparent 100%); }")
val Focus = CssUtility("focus", null)
val Clickable = CssUtility("clickable", null)
val PointerEventsAuto = CssUtility("pointer-events-auto", ".pointer-events-auto { pointer-events: auto; }")
val PointerEventsNone = CssUtility("pointer-events-none", ".pointer-events-none { pointer-events: none; }")

// object DisplayNone : Modifier { override val identifier = "display-none" }
// object Opacity1: Modifier { override val identifier = "opacity-1" }
// object OpacityMost: Modifier { override val identifier = "opacity-most" }
// object OpacityHalf: Modifier { override val identifier = "opacity-half" }
// object OpacitySome: Modifier { override val identifier = "opacity-some" }
// object Dim: Modifier { override val identifier = OpacityMost.identifier }
// object NoDim: Modifier { override val identifier = "no-dim" }
// object Glow: Modifier { override val identifier = "glow" }
// object GlowShadow: Modifier { override val identifier = "glow-shadow" }
// object GlowBackground: Modifier { override val identifier = "glow-background" }
// object CircleShape: Modifier { override val identifier = "circle-shape" }
// object CircleClip: Modifier { override val identifier = "circle-clip" }
// object BorderRadius0: Modifier { override val identifier = "border-radius-0" }
// object BorderRadius1: Modifier { override val identifier = "border-radius-1" }
// object BorderRadius2: Modifier { override val identifier = "border-radius-2" }
// object BorderRadius4: Modifier { override val identifier = "border-radius-4" }
// object Accent: Modifier { override val identifier = "accent" }
// object Primary: Modifier { override val identifier = "primary" }
// object Secondary: Modifier { override val identifier = "secondary" }
// object Danger: Modifier { override val identifier = "danger" }
// object Clickable: Modifier { override val identifier = "clickable" }
// object OverflowHidden: Modifier { override val identifier = "overflow-hidden"}
// object OverflowWrapAnywhere: Modifier { override val identifier = "overflow-wrap-anywhere" }
// object OverflowClip: Modifier { override val identifier = "overflow-clip" }
// object Focus: Modifier { override val identifier = "focus" }
// object PrimaryBg: Modifier { override val identifier = "primary-bg" }
// object PrimaryCardBg: Modifier { override val identifier = "primary-card-bg" }
// object BackgroundImage: Modifier { override val identifier = "background-image" }
// object SolidBg: Modifier { override val identifier = "solid-bg" }
// object BlurBackdrop: Modifier { override val identifier = "blur-backdrop" }
// object FadeBottom: Modifier { override val identifier = "fade-bottom" }