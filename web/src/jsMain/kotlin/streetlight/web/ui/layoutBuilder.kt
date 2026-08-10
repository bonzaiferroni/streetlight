package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.setPopoverTarget
import koala.model.fieldOf
import koala.model.narrow
import koala.model.storeOf
import koala.model.toggle
import streetlight.model.data.EventsBlock
import streetlight.model.data.HeaderBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.MapBlock
import streetlight.model.data.RichTextBlock
import streetlight.model.data.TabContent
import streetlight.model.data.TabsBlock
import streetlight.model.data.TextBlock
import streetlight.web.layouts.buildEvents
import streetlight.web.layouts.buildHeader
import streetlight.web.layouts.buildImage
import streetlight.web.layouts.buildMap
import streetlight.web.layouts.buildRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.layoutBuilder(model: LayoutEditor, content: LocationContent) {
    containerBuilder(model, model.mainContainerId, content)
}

fun ViewScope.containerBuilder(model: LayoutEditor, containerId: Uuid, content: LocationContent) {
    val editor = model.getContainer(containerId)
    flowBlock(editor.blockIdsField) { blockIds ->
        column {
            blockZone(model, containerId, 0)
            blockIds.forEachIndexed { index, blockId ->
                blockBuilder(model, blockId, content)
                blockZone(model, containerId, index + 1)
            }
        }
    }
}

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: Uuid, content: LocationContent) {
    val editor = model.getBlock(blockId)
    box {
        when (val block = editor.blockField.now) {
            EventsBlock -> buildEvents(content)
            HeaderBlock -> buildHeader(content)
            is ImageBlock -> buildImage(block)
            MapBlock -> buildMap(content.location.geoPoint)
            is RichTextBlock -> buildRichText(block)
            is TextBlock -> textBuilder(editor)
            is TabsBlock -> tabsBuilder(model, editor, content)
        }
    }
}

fun ViewScope.blockZone(model: LayoutEditor, containerId: Uuid, index: Int) {
    button(SvgFile.Plus, {
        model.addBlock(TextBlock("My Text"), containerId, index)
    })
}

fun ViewScope.editBlock(content: ViewScope.(Boolean) -> Unit) {
    val isEditingField = storeOf(false)
    flowBlock(isEditingField) { isEditing ->
        box {
            content(isEditing)
            button(SvgFile.Edit, isEditingField::toggle, modify(JustifySelfEnd, Height3))
        }
    }
}

fun ViewScope.textBuilder(editor: BlockEditor) {
    val textField = editor.mutableFieldOf<TextBlock, String>({ it.text }) { copy(text = it) }
    editBlock { isEditing ->
        if (isEditing) {
            textField(textField)
        } else {
            textBlock(textField.now)
        }
    }
}

fun ViewScope.tabsBuilder(model: LayoutEditor, editor: BlockEditor, content: LocationContent) {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    val popoverId = Id("tabs-popover")
    popoverCard(popoverId) {
        flowBlock(editor.refreshField) {
            column {
                editor.containerKeys?.forEach { (tabName, containerId) ->
                    val isEditingField = storeOf(false)
                    flowBlock(isEditingField, modify(Height5)) { isEditing ->
                        if (isEditing) {
                            val tabNameField = storeOf(tabName)
                            textField(tabNameField, onEnter = {
                                editor.renameContainer(containerId, tabNameField.now)
                            })
                        } else {
                            row(modify(AlignItemsCenter, Height5)) {
                                textBlock(tabName, modify(Flex1))
                                button(SvgFile.Edit, { isEditingField.toggle() })
                                button(SvgFile.Minus, {
                                    editor.removeContainer(containerId)
                                })
                            }
                        }
                    }
                }
                button(SvgFile.Plus, {
                    editor.addContainer(TabContent("new tab", emptyList()))
                })
            }
        }
    }

    column {
        row(modify(JustifyContentEnd)) {
            button("tabs", mod = modify(Zen)) {
                setPopoverTarget(popoverId)
            }
        }
        flowBlock(editor.refreshField) {
            tabs {
                editor.containerKeys?.forEach { (name, containerId) ->
                    tab(name) {
                        containerBuilder(model, containerId, content)
                    }
                }
            }
        }
    }
}