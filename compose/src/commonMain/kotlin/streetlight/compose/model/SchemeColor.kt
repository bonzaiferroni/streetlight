package streetlight.compose.model

/**
 * A color named for its role in the theme, resolved by `KoalaColors.colorOf`. The context's is held by
 * [streetlight.compose.view.LocalSchemeColor]. The cousin of the web's `ThemeColor`.
 */
enum class SchemeColor {
    Ink,
    Muted,
    Accent,
    Primary,
    Galaxy,
    Event,
    Location,
    City,
    Media,
}
