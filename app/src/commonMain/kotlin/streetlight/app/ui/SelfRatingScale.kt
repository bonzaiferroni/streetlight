package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import compose.icons.TablerIcons
import compose.icons.tablericons.Star
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.selected
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import pondui.utils.darken
import streetlight.model.data.*

@Composable
fun SelfRatingScale(
    rating: SelfRating?,
    setRating: (SelfRating) -> Unit
) {
    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        Label("Self Rating")
        Row(
            gap = 1,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.height(IntrinsicSize.Max)
        ) {
            SelfRating.entries.forEach { entry ->
                val isSelected = entry == rating
                Column(
                    gap = 1,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                        .actionable { setRating(entry) }
                ) {
                    val color = when {
                        rating == null -> Pond.localColors.content
                        rating.ordinal >= entry.ordinal -> Pond.colors.glow
                        else -> Pond.localColors.contentDim.darken()
                    }
                    Icon(TablerIcons.Star, color = color)
                    Label(
                        entry.label,
                        modifyStyle = { it.copy(textAlign = TextAlign.Center) },
                        color = when {
                            isSelected -> Pond.localColors.content
                            rating == null -> Pond.localColors.contentDim
                            else -> Pond.localColors.contentDim.darken(.3f)
                        },
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SelfRatingScalePreview() {
    MultiPreview {
        PreviewFrame("null rating") {
            SelfRatingScale(null, { })
        }
        PreviewFrame("1 star rating") {
            SelfRatingScale(SelfRating.FirstSteps, { })
        }
        PreviewFrame("3 star rating") {
            SelfRatingScale(SelfRating.ComingAlong, { })
        }
        PreviewFrame("5 star rating") {
            SelfRatingScale(SelfRating.Banger, { })
        }
    }
}