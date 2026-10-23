package streetlight.web.view

import kampfire.model.MutableTap
import koala.modifier.setPopoverTarget
import kotlin.uuid.Uuid
import koala.page.Id
import kampfire.model.toAbsoluteUrlOrNull
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.component.heading4
import koala.component.spacer
import koala.component.textBlock
import kampfire.model.append
import kampfire.model.removeAt
import kampfire.model.replaceAt
import kampfire.model.storeOf
import kampfire.model.toggle
import koala.component.heading5
import koala.model.ExtraLink
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.MenuAction
import koala.model.toSvg
import streetlight.model.data.SkillTag
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.model.SkillEditor

fun ViewScope.linksFormSection(
    linksState: MutableTap<List<ExtraLink>>,
    instructions: String = "You may provide your original source, a youtube video, or any useful link."
) {
    context(linksState) {
        formSection("additional links") {
            val addId = Id(Uuid.random().toString())
            linkPopover(addId, ExtraLink.Empty) { linksState.append(it) }

            card(Padding(2)) {
                actionSection(
                    label = "Links",
                    actions = listOf(
                        MenuAction("add new", config = { setPopoverTarget(addId, true) }) { }
                    ),
                    heading = { heading5(it) }
                ) {
                    flowBlock(linksState) { links ->
                        links.forEachIndexed { index, link ->
                            linkRow(link, index)
                        }
                        if (links.isEmpty()) {
                            textBlock(instructions, modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
                        }
                    }
                }
            }
        }
    }
}

context(linksState: MutableTap<List<ExtraLink>>,)
private fun ViewScope.linkRow(link: ExtraLink, index: Int) {
    val editId = Id(Uuid.random().toString())
    linkPopover(editId, link) { linksState.replaceAt(index, it) }
    val actions = listOf(
        MenuAction("Edit", config = { setPopoverTarget(editId, true) }) { },
        MenuAction("Remove") { linksState.removeAt(index) },
    )
    val svg = link.toSvg()
    row(modify(AlignItemsCenter)) {
        icon(svg.large, Height(5))
        column(Gap0) {
            textBlock(link.label, Bold)
            textBlock(link.url.value, InkDimFg)
        }
        menuActionsRow(actions)
    }
}

/** A popover with [id] holding fields for a link's label and url, starting from [link], that hands a valid link to [onSave]. */
private fun ViewScope.linkPopover(id: Id, link: ExtraLink, onSave: (ExtraLink) -> Unit) {
    popover(id) { popover ->
        val labelState = storeOf(link.label)
        val urlState = storeOf(link.url.value)
        val save = save@{
            val label = labelState.now.takeIf { it.isNotBlank() } ?: return@save
            val url = urlState.now.toAbsoluteUrlOrNull() ?: return@save
            onSave(ExtraLink(label, url))
            labelState.set(link.label)
            urlState.set(link.url.value)
            popover.close()
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(labelState, "label", Flex1, onEnterSubmit = save)
            textField(urlState, "url", Flex3, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}
