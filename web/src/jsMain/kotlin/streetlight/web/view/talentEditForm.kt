package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.checkBox
import koala.dom.component.dropMenu
import koala.dom.component.row
import koala.dom.component.textField
import koala.model.MenuAction
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.data.TalentTag
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.TalentConfig
import streetlight.web.model.TalentEditor

/** The form of the talent [model] edits, which returns to [config]'s list once saved or abandoned. */
context(model: TalentEditor, config: TalentConfig)
fun ViewScope.talentEditForm() = formColumn {
    val messages = MessageStore()
    val heading = if (model.stateNow.edit.talentId == null) "New Talent" else "Edit Talent"

    formSection(heading) {
        textField(model.nameField, "name", maxLength = 50)
        row(AlignItemsCenter) {
            dropMenu(model.levelField)
            textField(model.yearField, "year started", Flex1)
        }
        textField(model.descriptionField, "description")
    }
    formSection("Tags") {
        row(modify(FlexWrap, AlignItemsCenter)) {
            TalentTag.entries.forEach { tag ->
                checkBox(model.tagField(tag), tag.label)
            }
        }
    }
    formSubmit(
        label = "Save",
        onClick = {
            launchEffect {
                val talent = model.submitSuspend(messages) ?: return@launchEffect
                config.finishTalentEdit(talent)
            }
        },
        messenger = messages,
        back = MenuAction("back", onClick = config::cancelFocus),
    )
}
