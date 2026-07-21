package streetlight.web.ui

import koala.SvgFile
import koala.css.AlignItemsEnd
import koala.css.Danger
import koala.css.Dim
import koala.css.Flex1
import koala.css.Flex3
import koala.css.Gap1
import koala.css.Gap2
import koala.css.Height5
import koala.css.JustifyContentCenter
import koala.css.OpacityHigh
import koala.css.Width100P
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.column
import koala.dom.defaultMagic
import koala.dom.flowBlock
import koala.dom.icon
import koala.dom.indexedItemsBlock
import koala.dom.row
import koala.dom.textField
import koala.html.heading4
import koala.html.spacer
import koala.html.textBlock
import koala.model.dedup
import koala.model.fieldOf
import koala.model.storeOf
import streetlight.model.data.ExtraLink
import streetlight.web.model.EventEditor


fun ViewScope.eventLinks(model: EventEditor) {
    val linksFlow = model.stateFlow.dedup { it.edit.links ?: emptyList() }

    val editState = storeOf(LinkEditState())
    val linkEditIndexFlow = editState.fieldOf { it.index }
    val labelFlow = editState.flow.dedup { it.link.label }
    val urlFlow = editState.flow.dedup { it.link.url }

    fun setLink(provider: (ExtraLink) -> ExtraLink) { editState.setValue { it.copy(link = provider(it.link)) }}
    fun setLabel(value: String) { setLink { it.copy(label = value) } }
    fun setUrl(value: String) { setLink { it.copy(url = value) } }
    fun finalizeEdit() {
        val link = editState.now.link.takeIf { it.isValid } ?: return
        val index = editState.now.index ?: error("no edit index")
        model.editLink(index, link)
        editState.setValue { LinkEditState() }
    }
    fun addLink() {
        val index = model.stateNow.edit.links?.size ?: 0
        model.addLink(ExtraLink.Blank)
        editState.setValue { it.copy(index = index) }
    }

    column(modify(Gap2)) {
        column(modify(Gap1)) {
            textBlock("Is there more information about this event somewhere out there?", modify(OpacityHigh))
            textField(model.urlField, "Link", modify(Width100P))
        }
        column(modify(Gap1)) {
            textBlock("Want to give a shout out to the original place where you found the event?", modify(OpacityHigh))
            row {
                textField(model.originalSourceLabelField, "Source label", modify(Flex1))
                textField(model.originalSourceUrlField, "Source url", modify(Flex3))
            }
        }
        row(modify(AlignItemsEnd)) {
            column(modify(Flex1)) {
                heading4("Additional Links")
                textBlock("You may provide your original source, a youtube video, or any useful link.", modify(Dim))
            }
            button("➕ Add link", onClick = ::addLink)
        }
        indexedItemsBlock(linksFlow, defaultMagic) { (linkIndex, link) ->
            flowBlock(linkEditIndexFlow, modify(Height5, JustifyContentCenter)) { index ->
                val isEdit = index == linkIndex
                if (isEdit) {
                    row() {
                        // td: fix and reintroduce
                        // textField("label", ::setLabel, labelFlow)
                        // textField("url", ::setUrl, urlFlow, modify(Flex1))
                        icon(SvgFile.Check, ::finalizeEdit, modify(Height5))
                    }
                } else {
                    row() {
                        textBlock(link.label)
                        textBlock(link.url, modify(Dim))
                        spacer(modify(Flex1))
                        icon(SvgFile.Trash, onClick = { model.removeLink(link) }, modify(Dim, Danger))
                        icon(SvgFile.Edit, onClick = { editState.setValue{ it.copy(index = linkIndex, link = link)} }, modify(Dim))
                    }
                }
            }
        }
    }
}

private data class LinkEditState(
    val index: Int? = null,
    val link: ExtraLink = ExtraLink("", ""),
)