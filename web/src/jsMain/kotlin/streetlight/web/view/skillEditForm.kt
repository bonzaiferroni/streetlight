package streetlight.web.view

import kampfire.model.storeOf
import koala.component.heading5
import koala.dom.AppendScope
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.sideTabs
import koala.dom.markdown.markdownEditor
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import streetlight.model.data.SkillCategory
import streetlight.model.data.SkillTag
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.model.SkillEditor

/** The form of the skill [model] edits. */
context(model: SkillEditor)
fun ViewScope.skillEditForm() = formColumn {
    val heading = if (model.stateNow.edit.skillId == null) "New Skill" else "Edit Skill"

    formRow {
        formSection(heading) {
            textField(model.nameState, "name", maxLength = 50)
            row(AlignItemsCenter) {
                dropMenu(model.levelState)
                textField(model.yearState, "year started", Flex1)
            }
            skillTagMenu()
        }
        context(model.imageEditor) {
            imageFormSection("This image will appear with the skill.")
        }
    }
    formSection("Description") {
        markdownEditor(model.descriptionState, "description", MinHeight(32))
    }
    context(model.interviewEditor) {
        interviewForm()
    }
}

context(model: SkillEditor)
private fun ViewScope.skillTagMenu() {
    val id = Id("skill-tag-popover")
    val categoryState = storeOf(SkillCategory.Music)
    popover(id) { popover ->
        flowBlock(model.tagsState) { currentTags ->
            sideTabs(categoryState, modify(MaxWidth(72), MaxHeight(48), CardBg)) { category ->
                row(modify(FlexWrap, JustifyContentCenter, Padding(1))) {
                    category.tags.forEach { tag ->
                        val isTagged = tag in currentTags
                        val buttonMod = if (isTagged) Primary else Zen
                        button(tag.label, {
                            model.toggleTag(tag)
                            if (!isTagged) popover.close()
                        }, buttonMod, flair = tag.svg)
                    }
                }
            }
        }
    }
    card(Padding(2)) {
        actionSection(
            label = "Tags",
            actions = listOf(
                MenuAction("add new", config = { setPopoverTarget(id, true) }) {  }
            ),
            heading = { heading5(it) }
        ) {
            flowBlock(model.tagsState) { tags ->
                tags.forEach { tag ->
                    skillRow(tag)
                }
                if (tags.isEmpty()) {
                    textBlock("Add a tag that fits your skill.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
                }
            }
        }
    }
}

context(model: SkillEditor)
private fun AppendScope.skillRow(tag: SkillTag) {
    val actions = listOf(MenuAction("Remove") { model.toggleTag(tag) })
    row(modify(AlignItemsCenter)) {
        icon(tag.svg.small)
        heading5(tag.label)
        menuActionsRow(actions)
    }
}