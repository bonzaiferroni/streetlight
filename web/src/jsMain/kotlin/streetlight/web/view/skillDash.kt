package streetlight.web.view

import koala.SvgFile
import koala.component.heading5
import koala.component.icon
import koala.component.navigation
import koala.dom.ViewScope
import koala.dom.component.column
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.ui.SkillRoute
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.menuActionButton
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.SkillConfig
import streetlight.web.model.SkillEditor
import streetlight.web.model.api
import streetlight.web.model.toaster

/** The star's skills, or the editor of the skill [SkillConfig] has staged as its focus. */
fun ViewScope.skillDash() {
    val model = SkillConfig(contentScope, api, toaster)
    context(model) {
        flowBlock(model.focusState) { focus ->
            when (focus) {
                null -> skillsSection()
                else -> column {
                    val editor = SkillEditor(focus, contentScope, api)
                    val messages = MessageStore()
                    context(editor) {
                        val label = if (focus.skillId == null) "New Skill" else "Update Skill"
                        actionSection(label, listOf(
                            MenuAction("back", onClick = model::cancelFocus)
                        )) {
                            skillEditForm()
                        }

                        formSubmit(
                            label = "Save",
                            onClick = {
                                launchEffect {
                                    val skill = editor.submitSuspend(messages) ?: return@launchEffect
                                    model.finishSkillEdit(skill)
                                }
                            },
                            messenger = messages,
                            back = MenuAction("back", onClick = model::cancelFocus),
                        )
                    }
                }
            }
        }
    }
}

/** The star's skills, each with a button that stages its edit, under a button for a new one. */
context(config: SkillConfig)
private fun ViewScope.skillsSection() = actionSection("Skills", listOf(
    MenuAction("New", onClick = config::startNewSkill)
)) {
    flowBlock(config.skillsState) { skills ->
        if (skills == null) return@flowBlock
        if (skills.isEmpty()) {
            centeredText("No skills yet.")
            return@flowBlock
        }
        column(Gap2Px) {
            skills.forEach { skill ->
                row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
                    val svg = skill.tags?.firstOrNull()?.svg ?: SvgFile.Award
                    icon(svg.animated, Height(5))
                    navigation(SkillRoute(skill.skillId), modify(Flex1)) {
                        column(modify(Gap0)) {
                            heading5(skill.name)
                            skill.tags?.let { tags ->
                                textBlock(tags.joinToString { it.label }, modify(TextSmall, OpacityHalf))
                            }
                        }
                    }
                    textBlock(skill.skillLevel.label, modify(TextUppercase, TextSmall, OpacityHigh))
                    menuActionButton(MenuAction("Edit") { config.startSkillEdit(skill) })
                }
            }
        }
    }
}
