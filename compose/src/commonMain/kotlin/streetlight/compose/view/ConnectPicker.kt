package streetlight.compose.view

import streetlight.compose.modifier.pointerClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import koala.SvgFile
import koala.SvgPack
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.shineBg
import streetlight.model.data.OnboardType

/**
 * The four ways to connect, one quadrant per [OnboardType] in a 4:3 grid, each its animated icon over its label,
 * calling [onPick] with its type. The cousin of the web's `connectPicker`.
 */
@Composable
fun ConnectPicker(modifier: Modifier = Modifier, onPick: (OnboardType) -> Unit) {
    val theme = currentTheme
    Column(
        modifier.aspectRatio(4f / 3f).borderRadius(2, moonShadow = true, outline = Outline.Mid).shineBg(),
        verticalArrangement = Arrangement.spacedBy(theme.line),
    ) {
        OnboardType.entries.chunked(2).forEach { pair ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(theme.line)) {
                pair.forEach { type -> ConnectOption(type, Modifier.weight(1f).pointerClick { onPick(type) }) }
            }
        }
    }
}

/** A quadrant of the picker: the icon of [type] over its label, centered. */
@Composable
private fun ConnectOption(type: OnboardType, modifier: Modifier) {
    val theme = currentTheme
    Column(
        modifier.fillMaxSize().cardBg(),
        verticalArrangement = Arrangement.spacedBy(theme.unit, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedIcon(type.svg.animated, color = { theme.colors.ink }, size = theme.unit * IconUnits)
        BasicText(type.label.uppercase(), style = theme.text.small, color = { theme.colors.ink })
    }
}

/** The icon of this connect type, as the web's. */
private val OnboardType.svg: SvgPack get() = when (this) {
    OnboardType.BusinessOwner -> SvgFile.BuildingStore
    OnboardType.Performer -> SvgFile.AcousticGuitar
    OnboardType.Developer -> SvgFile.Code
    OnboardType.EventHost -> SvgFile.Speakerphone
}

/** The width of an option's icon in units, the web's `Width(5)`. */
private const val IconUnits = 5
