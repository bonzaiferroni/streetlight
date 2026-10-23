package streetlight.web.layout

import koala.SvgFile
import koala.component.*
import koala.markdown.markdown
import koala.modifier.*
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.Skill
import streetlight.model.ui.SkillProfileRoute
import streetlight.web.view.ConnectStyle

/** The skills section of a star's page, holding their [skills]. */
fun FlowContent.skillsSection(skills: List<Skill>) {
    section("Skills") {
        // MaxWidth(80.pct)
        row(modify(FlexWrap, Gap2Px, JustifyContentCenter)) {
            skills.forEachIndexed { index, skill ->
                skillCard(skill, Palette.cycle(index).mod)
            }
        }
    }
}

/** A card linking to [skill]'s page: the icon of its first tag in a badge, its name, and its description faded at the bottom. */
private fun FlowContent.skillCard(skill: Skill, colorMod: Class) {
    val svg = skill.tags?.firstOrNull()?.svg ?: SvgFile.Award
    navigation(SkillProfileRoute(skill.skillId)) {
        column(modify(AlignItemsCenter, TextAlignCenter, Gap(2), Padding(2), ZenBg, Width(20), Height(40))) {
            box(modify(ConnectStyle.IconBadge, PlaceItemsCenter, Width(16), Aspect1, colorMod)) {
                icon(svg.animated, modify(Width(8)))
            }
            box(modify(Height(4), PlaceItemsCenter)) {
                heading6(skill.name)
            }
            skill.description?.let {
                markdown(it, modify(Flex1, OpacityHigh, OverflowHidden, FadeBottom, TextSmall))
            }
        }
    }
}
