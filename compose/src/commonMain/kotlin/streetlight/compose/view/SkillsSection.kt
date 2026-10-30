package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import koala.SvgFile
import koala.modifier.Palette
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.modifier.fadeClip
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.zenBg
import streetlight.compose.utility.toColor
import streetlight.model.data.Skill

/**
 * The [skills] of a star under their heading, a card each, wrapping to rows and centered, each in the next color of the
 * [Palette]. The cousin of the web's `skillsSection`, its cards unlinked until the app has a skill's page.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(skills: List<Skill>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    Column(1, modifier.fillMaxWidth()) {
        BasicText(
            "Skills",
            Modifier.fillMaxWidth().filigree(),
            style = theme.text.heading3.copy(textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(theme.line, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(theme.line),
        ) {
            skills.forEachIndexed { index, skill -> SkillCard(skill, Palette.cycle(index).rgb.toColor()) }
        }
    }
}

/** A card of [skill]: the icon of its first tag in [color] on a badge, its name, and its description faded at the bottom. */
@Composable
private fun SkillCard(skill: Skill, color: Color) {
    val theme = currentTheme
    val svg = skill.tags?.firstOrNull()?.svg ?: SvgFile.Award
    Column(
        2,
        Modifier.width(theme.unit * CardWidthUnits).height(theme.unit * CardHeightUnits).zenBg().padding(2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(svg, color)
        Box(Modifier.height(theme.unit * NameUnits), contentAlignment = Alignment.Center) {
            BasicText(skill.name, style = theme.text.heading6.copy(textAlign = TextAlign.Center), color = { theme.colors.ink })
        }
        skill.description?.let { description ->
            val text = remember(description, theme.colors.primaryFg) { annotatedMarkdownOf(description, theme.colors.primaryFg) }
            BasicText(
                text,
                Modifier.fillMaxWidth().fadeClip(DescriptionUnits).opacityHigh(),
                style = theme.text.small.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
        }
    }
}

/** The size of a card in units, the web's `Width(20)` and `Height(40)`. */
private const val CardWidthUnits = 20
private const val CardHeightUnits = 40

/** The height of the name's box in units, the web's `Height(4)`. */
private const val NameUnits = 4

/** The height left to the description: the card less its padding, badge, name, and the gaps between them. */
private const val DescriptionUnits = CardHeightUnits - 2 * 2 - IconBadgeUnits - NameUnits - 2 * 2
