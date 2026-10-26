package streetlight.compose.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.fadeClip
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.schemeBg
import streetlight.model.data.EntityType
import streetlight.model.data.Galaxy
import streetlight.model.data.toDetails

/**
 * A small card of [galaxy], in the galaxy's scheme color: its image beside its name over its description, fading where
 * it reaches the clip, and its details below. The cousin of the web's `smallGalaxyCard`.
 */
@Composable
fun SmallGalaxyCard(galaxy: Galaxy, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val description = galaxy.description?.value
    CompositionLocalProvider(LocalSchemeColor provides EntityType.Galaxy) {
        Row(
            modifier.fillMaxWidth()
                .height(theme.unit * CardUnits)
                .borderRadius(2, moonShadow = true, outline = Outline.Low)
                .cardBg()
        ) {
            ContainImage(galaxy.image, Modifier.weight(1f).fillMaxHeight())
            Column(Modifier.weight(2f).fillMaxHeight()) {
                Column(Modifier.weight(1f).padding(1)) {
                    BasicText(
                        text = galaxy.name,
                        style = theme.text.heading5.copy(lineHeight = 1.em),
                        color = { theme.colors.ink },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = description ?: "Too mysterious for a description",
                        modifier = Modifier.fadeClip(DescriptionUnits),
                        style = theme.text.small.copy(fontStyle = if (description == null) FontStyle.Italic else null),
                        color = { if (description == null) theme.colors.muted else theme.colors.ink },
                    )
                }
                CellGrid(galaxy.toDetails(), Modifier.borderRadius(0, moonShadow = true).schemeBg())
            }
        }
    }
}

/** The height of the card in units, the web's `Height(16)`. */
private const val CardUnits = 16

/** The height the description is clipped at in units, the web's `MaxHeight(5)`. */
private const val DescriptionUnits = 5
