package koala.css

data class CssUtility(
    override val identifier: String,
    val definition: String?
    ): Modifier

// display utilities


// font utilities
object Bold: Modifier { override val identifier = "bold" }
object Italic: Modifier { override val identifier = "italic" }
object SmallFont: Modifier { override val identifier = "small-font" }
object LargeFont: Modifier { override val identifier = "large-font" }
object Heading1: Modifier { override val identifier = "heading-1" }
object Heading2: Modifier { override val identifier = "heading-2" }
object WhiteSpaceNormal: Modifier { override val identifier = "white-space-normal" }
object LineHeight1: Modifier { override val identifier = "line-height-1" }