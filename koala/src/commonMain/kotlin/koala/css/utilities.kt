package koala.css

data class CssUtility(
    override val identifier: String,
    val definition: String?
    ): Modifier

// display utilities
object DisplayNone : Modifier { override val identifier = "display-none" }
object Opacity1: Modifier { override val identifier = "opacity-1" }
object OpacityMost: Modifier { override val identifier = "opacity-most" }
object OpacityHalf: Modifier { override val identifier = "opacity-half" }
object OpacitySome: Modifier { override val identifier = "opacity-some" }
object Dim: Modifier { override val identifier = OpacityMost.identifier }
object NoDim: Modifier { override val identifier = "no-dim" }
object Glow: Modifier { override val identifier = "glow" }
object GlowShadow: Modifier { override val identifier = "glow-shadow" }
object GlowBackground: Modifier { override val identifier = "glow-background" }
object CircleShape: Modifier { override val identifier = "circle-shape" }
object CircleClip: Modifier { override val identifier = "circle-clip" }
object BorderRadius0: Modifier { override val identifier = "border-radius-0" }
object BorderRadius1: Modifier { override val identifier = "border-radius-1" }
object BorderRadius2: Modifier { override val identifier = "border-radius-2" }
object BorderRadius4: Modifier { override val identifier = "border-radius-4" }
object Accent: Modifier { override val identifier = "accent" }
object Primary: Modifier { override val identifier = "primary" }
object Secondary: Modifier { override val identifier = "secondary" }
object Danger: Modifier { override val identifier = "danger" }
object Clickable: Modifier { override val identifier = "clickable" }
object OverflowHidden: Modifier { override val identifier = "overflow-hidden"}
object OverflowWrapAnywhere: Modifier { override val identifier = "overflow-wrap-anywhere" }
object OverflowClip: Modifier { override val identifier = "overflow-clip" }
object Focus: Modifier { override val identifier = "focus" }
object PrimaryBg: Modifier { override val identifier = "primary-bg" }
object PrimaryCardBg: Modifier { override val identifier = "primary-card-bg" }
object BackgroundImage: Modifier { override val identifier = "background-image" }
object SolidBg: Modifier { override val identifier = "solid-bg" }
object BlurBackdrop: Modifier { override val identifier = "blur-backdrop" }
object FadeBottom: Modifier { override val identifier = "fade-bottom" }

// font utilities
object Bold: Modifier { override val identifier = "bold" }
object Italic: Modifier { override val identifier = "italic" }
object SmallFont: Modifier { override val identifier = "small-font" }
object LargeFont: Modifier { override val identifier = "large-font" }
object Heading1: Modifier { override val identifier = "heading-1" }
object Heading2: Modifier { override val identifier = "heading-2" }
object WhiteSpaceNormal: Modifier { override val identifier = "white-space-normal" }