package streetlight.web.view

import koala.component.textBlock
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSectionCard
import streetlight.web.model.SkillLinkEditor
import streetlight.web.model.toFlair
import kotlin.uuid.Uuid

/** The caller's skills the edited record is linked to, each removable, with a popover of the rest to add. */
context(model: SkillLinkEditor)
fun ViewScope.skillLinksSection() {
    val id = Id(Uuid.random().toString())
    popover(id) { popover ->
        flowBlock(model.unlinkedSkillsState) { skills ->
            if (skills == null) return@flowBlock
            column(Gap(1)) {
                skills.forEach { skill ->
                    button(skill.name, {
                        model.toggleSkill(skill.skillId)
                        popover.close()
                    }, Zen, flair = skill.toFlair())
                }
                if (skills.isEmpty()) {
                    textBlock("Every skill is linked. Add skills in your dashboard.", modify(TextSmall, OpacityHigh))
                }
            }
        }
    }
    actionSectionCard(
        label = "Skills",
        actions = listOf(
            MenuAction("add new", config = { setPopoverTarget(id, true) }) { }
        ),
    ) {
        flowBlock(model.linkedSkillsState) { skills ->
            if (skills == null) return@flowBlock
            skills.forEach { skill ->
                actionRow(skill, actions = listOf(MenuAction("Remove") { model.toggleSkill(skill.skillId) }))
            }
            if (skills.isEmpty()) {
                textBlock("Link this to the skills it shows.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            }
        }
    }
}
