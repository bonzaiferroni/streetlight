package streetlight.compose.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.schemeBg
import streetlight.compose.modifier.zenBg
import streetlight.model.data.Entity
import streetlight.model.data.EntityDetail
import streetlight.model.data.toEntityType
import streetlight.compose.model.toRoute

/**
 * The header of [entity] on its page, in the entity's scheme color: its image beside its title, [descriptor] and
 * subtitle, side by side from the web's 600px container width and stacked below it, then its [details] and its
 * description. The cousin of the web's `pageHeader`, the header every page has by default.
 */
@Composable
fun EntityHeader(entity: Entity, descriptor: String, details: List<EntityDetail>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val route = entity.toRoute()
    CompositionLocalProvider(LocalSchemeColor provides theme.colors.colorOf(entity.toEntityType())) {
        Column(modifier.sharedPart(route, SharedPart.Frame).fillMaxWidth().borderRadius(2, moonShadow = true, outline = Outline.Mid).zenBg()) {
            BoxWithConstraints {
                val image = @Composable { imageModifier: Modifier -> ContainImage(entity.image, imageModifier.sharedPart(route, SharedPart.Image)) }
                val title = @Composable { titleModifier: Modifier -> HeaderTitle(entity, descriptor, titleModifier) }
                if (maxWidth >= SideBySideWidth) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (entity.image != null) image(Modifier.weight(1f).aspectRatio(HeaderImageRatio))
                        title(Modifier.weight(1f))
                    }
                } else {
                    Column {
                        if (entity.image != null) image(Modifier.fillMaxWidth().aspectRatio(HeaderImageRatio))
                        title(Modifier.fillMaxWidth())
                    }
                }
            }
            if (details.isNotEmpty()) CellGrid(details, Modifier.borderRadius(0, moonShadow = true).schemeBg())
            entity.description?.let { description ->
                val text = remember(description, theme.colors.primaryFg) { annotatedMarkdownOf(description, theme.colors.primaryFg) }
                BasicText(text, Modifier.padding(2), style = theme.text.paragraph, color = { theme.colors.ink })
            }
        }
    }
}

/** The title of [entity] over [descriptor] in a filigree and its subtitle, centered. */
@Composable
private fun HeaderTitle(entity: Entity, descriptor: String, modifier: Modifier) {
    val theme = currentTheme
    Column(modifier.padding(2), horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            entity.label,
            Modifier.sharedPart(entity.toRoute(), SharedPart.Title),
            style = theme.text.heading2.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
        BasicText(
            descriptor,
            Modifier.fillMaxWidth().filigree(),
            style = theme.text.regular.copy(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
            color = { theme.colors.ink.copy(alpha = theme.opacity.half) },
        )
        entity.sublabel?.let {
            BasicText(it, Modifier.opacityHigh(), style = theme.text.regular.copy(textAlign = TextAlign.Center), color = { theme.colors.ink })
        }
    }
}

/** The width from which the image and the title sit side by side: the web's `ContainerMdRow`. */
private val SideBySideWidth = 600.dp

/** The shape of the header's image. */
private const val HeaderImageRatio = 3f / 2f
