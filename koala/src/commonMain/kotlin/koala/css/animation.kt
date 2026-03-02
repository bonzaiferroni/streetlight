package koala.css

import kotlinx.css.*
import kotlinx.css.properties.*

@Deprecated("use reveal")
object Show: Modifier { override val value = "show" }
object FadeStack: Modifier { override val value = "fade-stack" }

// utilities
object Magic: Modifier { override val value = "magic" }
object Reveal: Modifier { override val value = "reveal" }
// object Hide: CssClass { override val value = "hide" }
object Blur: Modifier { override val value = "blur" }
object SlideX: Modifier { override val value = "slide-x" }
object SlideY: Modifier { override val value = "slide-y" }
object Scale: Modifier { override val value = "scale" }
