package streetlight.model.ui

/** The content of a view's top bar, drawn in a look shared by every view. */
sealed interface TopBar {
    /** The app's logo between its rays. */
    data object Logo : TopBar

    /** The view's [text] in a filigree, as a map's title. */
    data class Title(val text: String) : TopBar

    /** A title in the logo's two colors, [accent] then [primary], between the rays. */
    data class TwoTone(val accent: String, val primary: String) : TopBar
}
