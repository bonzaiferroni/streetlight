package streetlight.web.view

import kampfire.model.storeOf
import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.sideTabs
import koala.dom.markdown.markdownEditor
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import streetlight.model.data.TalentCategory
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.model.TalentEditor

/** The form of the talent [model] edits. */
context(model: TalentEditor)
fun ViewScope.talentEditForm() = formColumn {
    val heading = if (model.stateNow.edit.talentId == null) "New Talent" else "Edit Talent"

    formRow {
        formSection(heading) {
            textField(model.nameState, "name", maxLength = 50)
            row(AlignItemsCenter) {
                dropMenu(model.levelState)
                textField(model.yearState, "year started", Flex1)
            }
            talentTagMenu()
        }
        context(model.imageEditor) {
            imageFormSection("This image will appear with the talent.")
        }
    }
    formSection("Description") {
        markdownEditor(model.descriptionState, "description", MinHeight(32))
    }
    context(model.interviewEditor) {
        interviewForm()
    }
}

context(model: TalentEditor)
private fun ViewScope.talentTagMenu() {
    val id = Id("talent-tag-popover")
    val categoryState = storeOf(TalentCategory.Music)
    popover(id) {
        flowBlock(model.tagsState) { currentTags ->
            sideTabs(categoryState, modify(MaxWidth(72), MaxHeight(48), CardBg)) { category ->
                row(modify(FlexWrap, JustifyContentCenter, Padding(1), ScaleIn)) {
                    category.tags.forEach { tag ->
                        val buttonMod = if (tag in currentTags) Primary else Zen
                        button(tag.label, { model.toggleTag(tag) }, buttonMod, flair = tag.svg)
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
                    actionRow(tag.label, actions = listOf(MenuAction("Remove") { model.toggleTag(tag) }))
                }
            }
        }
    }
}