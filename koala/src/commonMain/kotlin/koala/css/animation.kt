package koala.css

@Deprecated("use reveal")
object Show: Modifier { override val identifier = "show" }

// utilities
object Magic: Modifier { override val identifier = "magic" }
object Reveal: Modifier { override val identifier = "reveal" }
// object Hide: CssClass { override val value = "hide" }
object Blur: Modifier { override val identifier = "blur" }
object SlideLeft: Modifier { override val identifier = "slide-left" }
object SlideUp: Modifier { override val identifier = "slide-up" }
object SlideRight: Modifier { override val identifier = "slide-right" }
object SlideDown: Modifier { override val identifier = "slide-down" }
object Scale: Modifier { override val identifier = "scale" }
