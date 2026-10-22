package streetlight.web.view

import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.column
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.dashAction
import streetlight.web.dom.layout.dashSection
import streetlight.web.model.TalentConfig
import streetlight.web.model.TalentEditor
import streetlight.web.model.api
import streetlight.web.model.toaster

/** The star's talents, or the editor of the talent [TalentConfig] has staged as its focus. */
fun ViewScope.talentDash() {
    val config = TalentConfig(contentScope, api, toaster)
    context(config) {
        flowBlock(config.focusState) { focus ->
            when (focus) {
                null -> talentsSection()
                else -> context(TalentEditor(focus, api)) {
                    val label = if (focus.talentId == null) "New Talent" else "Update Talent"
                    dashSection(label, listOf(
                        MenuAction("back", onClick = config::cancelFocus)
                    )) {
                        talentEditForm()
                    }
                }
            }
        }
    }
}

/** The star's talents, each with a button that stages its edit, under a button for a new one. */
context(config: TalentConfig)
private fun ViewScope.talentsSection() = dashSection("Talents", listOf(
    MenuAction("New", onClick = config::startNewTalent)
)) {
    flowBlock(config.talentsState) { talents ->
        if (talents == null) return@flowBlock
        if (talents.isEmpty()) {
            centeredText("No talents yet.")
            return@flowBlock
        }
        column {
            talents.forEach { talent ->
                row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
                    column(modify(Flex1, Gap0)) {
                        heading5(talent.name)
                        talent.tags?.let { tags ->
                            textBlock(tags.joinToString { it.label }, modify(TextSmall, OpacityHalf))
                        }
                    }
                    textBlock(talent.talentLevel.label, modify(TextUppercase, TextSmall, OpacityHigh))
                    dashAction(MenuAction("Edit") { config.startTalentEdit(talent) })
                }
            }
        }
    }
}
