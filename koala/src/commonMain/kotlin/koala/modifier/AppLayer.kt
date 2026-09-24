package koala.modifier

/** The layers of the page, each with the `z-index` its [mod] sets. */
enum class AppLayer(val zIndex: Int? = null) {
    Backdrop(-1),
    Body,
    Overlay(2),
    AboveOverlay(3);

    val mod: Modifier? = zIndex?.let { Css.ZIndex.of(it) }
}
