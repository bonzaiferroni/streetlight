package streetlight.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import compose.icons.TablerIcons
import compose.icons.tablericons.Bold
import compose.icons.tablericons.Check
import compose.icons.tablericons.CircleX
import compose.icons.tablericons.Rocket
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Button
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Row
import pondui.ui.theme.Pond
import pondui.ui.theme.defaultTheme
import pondui.ui.theme.useFamily
import pondui.utils.MultiPreview
import pondui.utils.MultiThemePreview
import pondui.utils.PreviewFrame
import streetlight.app.generated.resources.FiraSans_Bold
import streetlight.app.generated.resources.FiraSans_Italic
import streetlight.app.generated.resources.FiraSans_Light
import streetlight.app.generated.resources.FiraSans_Regular
import streetlight.app.generated.resources.Res

@Composable
@Preview
fun ButtonsPreview() {
    MultiPreview {
        PreviewFrame("Text Content") {
            FlowRow(1) {
                Button("Accent") { }
                Button("Primary", color = Pond.colors.primary) { }
                Button("Secondary", color = Pond.colors.secondary) { }
                Button("Warning", color = Pond.colors.negation) { }
            }
        }
        PreviewFrame("Icons") {
            Row(1) {
                Button(TablerIcons.Bold) { }
                Button(TablerIcons.Check, color = Pond.colors.primary) { }
                Button(TablerIcons.Rocket, color = Pond.colors.secondary) { }
                Button(TablerIcons.CircleX, color = Pond.colors.negation) { }
            }
        }
        PreviewFrame("Mixed") {
            Row(1) {
                Button(TablerIcons.Bold) { }
                Button("Accent") { }
                Button(TablerIcons.Check, color = Pond.colors.primary) { }
                Button("Primary", color = Pond.colors.primary) { }
            }
        }
    }
}

@Composable
@Preview
fun FontsPreview() {
    MultiThemePreview(
        "default" to defaultTheme(),
        "fira" to defaultTheme(
            baseFont = useFamily(Res.font.FiraSans_Regular),
            italicFont = useFamily(Res.font.FiraSans_Italic),
            lightFont = useFamily(Res.font.FiraSans_Light),
            boldFont = useFamily(Res.font.FiraSans_Bold),
        ),
        "fira" to defaultTheme(
            baseFont = useFamily(Res.font.FiraSans_Regular),
            italicFont = useFamily(Res.font.FiraSans_Italic),
            lightFont = useFamily(Res.font.FiraSans_Light, weight = FontWeight.Light),
            boldFont = useFamily(Res.font.FiraSans_Bold, weight = FontWeight.Bold),
        ),
    )
}

@Composable
@Preview
fun RejectFontsPreview() {
    MultiThemePreview(
//        "inter" to defaultTheme(
//            baseFont = useFamily(Res.font.Inter_18pt_Regular),
//            h1Font = useFamily(Res.font.Inter_28pt_Light, FontWeight.Light),
//            h2Font = useFamily(Res.font.Inter_24pt_Light, FontWeight.Light),
//            h4Font = useFamily(Res.font.Inter_18pt_Light, FontWeight.Light),
//        ),
//        "lato" to defaultTheme(
//            baseFont = useFamily(Res.font.Lato_Regular),
//            italicFont = useFamily(Res.font.Lato_Italic),
//            lightFont = useFamily(Res.font.Lato_Light),
//            boldFont = useFamily(Res.font.Lato_Bold),
//        ),
//        "merriweather" to defaultTheme(
//            baseFont = useFamily(Res.font.MerriweatherSans_Regular),
//            italicFont = useFamily(Res.font.MerriweatherSans_Italic),
//            lightFont = useFamily(Res.font.MerriweatherSans_Light),
//            boldFont = useFamily(Res.font.MerriweatherSans_Bold),
//        ),
//        "rambla" to defaultTheme(
//            baseFont = useFamily(Res.font.Rambla_Regular),
//            italicFont = useFamily(Res.font.Rambla_Italic),
//            boldFont = useFamily(Res.font.Rambla_Bold),
//        ),
//        "tuffy" to defaultTheme(
//            baseFont = useFamily(Res.font.Tuffy_Regular),
//            italicFont = useFamily(Res.font.Tuffy_Italic),
//            boldFont = useFamily(Res.font.Tuffy_Bold),
//        ),
//        "alan" to defaultTheme(
//            baseFont = useFamily(Res.font.AlanSans_Regular),
//            lightFont = useFamily(Res.font.AlanSans_Light),
//            boldFont = useFamily(Res.font.AlanSans_Bold),
//        ),
//        "bitcount" to defaultTheme(
//            baseFont = useFamily(Res.font.Bitcount_Variable),
//        ),
//        "dancing script" to defaultTheme(
//            baseFont = useFamily(Res.font.DancingScript_Regular),
//            boldFont = useFamily(Res.font.DancingScript_Bold),
//        ),
//        "Oswald" to defaultTheme(
//            baseFont = useFamily(Res.font.Oswald_Regular),
//            lightFont = useFamily(Res.font.Oswald_Light),
//            boldFont = useFamily(Res.font.Oswald_Bold),
//        ),
//        "playfair display" to defaultTheme(
//            baseFont = useFamily(Res.font.PlayfairDisplay_Regular),
//            italicFont = useFamily(Res.font.PlayfairDisplay_Italic),
//            boldFont = useFamily(Res.font.PlayfairDisplay_Bold),
//        ),
//        "quicksand" to defaultTheme(
//            baseFont = useFamily(Res.font.Quicksand_Regular),
//            lightFont = useFamily(Res.font.Quicksand_Light),
//            boldFont = useFamily(Res.font.Quicksand_Bold),
//        ),
//        "smooch" to defaultTheme(
//            baseFont = useFamily(Res.font.SmoochSans_Regular),
//            lightFont = useFamily(Res.font.SmoochSans_Light),
//            boldFont = useFamily(Res.font.SmoochSans_Bold),
//        ),
//        "spectral" to defaultTheme(
//            baseFont = useFamily(Res.font.Spectral_Regular),
//            lightFont = useFamily(Res.font.Spectral_Light),
//            boldFont = useFamily(Res.font.Spectral_Bold),
//            italicFont = useFamily(Res.font.Spectral_Italic),
//        ),
//        "titillium web" to defaultTheme(
//            baseFont = useFamily(Res.font.TitilliumWeb_Regular),
//            lightFont = useFamily(Res.font.TitilliumWeb_Light),
//            boldFont = useFamily(Res.font.TitilliumWeb_Bold),
//            italicFont = useFamily(Res.font.TitilliumWeb_Italic),
//        ),
    )
}