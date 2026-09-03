package streetlight.web.ui

import koala.Image
import koala.SvgFile
import koala.css.*
import koala.dom.*
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toggle
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.layouts.renderHeading
import streetlight.web.layouts.renderImage
import streetlight.web.layouts.renderRichText
import streetlight.web.layouts.toMod
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: BlockId) {
    val editor = model.getBlock(blockId)
    when (val block = editor.blockState.now) {
        HeaderBlock, EventsBlock, MapBlock -> blockBuilder(editor)
        is HeadingBlock -> headingBuilder(editor)
        is ImageBlock -> imageBuilder(editor)
        is GalleryBlock -> galleryBuilder(editor)
        is RichTextBlock -> richTextBuilder(editor)
        is TextBlock -> textBuilder(editor)
        is TabsBlock -> tabsBuilder(editor)
        is ColumnsBlock -> columnBuilder(editor)
    }
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
    content: FlowContent.() -> Unit
) = column {
    editorRow(editor)
    content()
}

fun ViewScope.blockBuilder(
    editor: BlockEditor,
) {
    val blockType = editor.block.blockType
    blockBuilder(editor) {
        buildContentBlock(blockType)
    }
}

fun ViewScope.buildContentBlock(type: BlockType) {
    box(modify(Outline, BorderRadius1, ZenBg, Height48)) {
        textBlock("${type.label} content", modify(TextUppercase, TextSmall, OpacityHigh, PlaceSelfCenter))
    }
}

fun ViewScope.tabsBuilder(editor: BlockEditor) {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        editorRow(editor) {
            flowBlock(editor.refreshState) {
                column {
                    editor.childIds.forEach { containerId ->
                        val tabName = editor.model.getContainer(containerId).name
                        val isEditingField = storeOf(false)
                        flowBlock(isEditingField, modify(Height5)) { isEditing ->
                            if (isEditing) {
                                val tabNameField = storeOf(tabName)
                                textField(tabNameField, onEnterSubmit = {
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

        flowBlock(editor.refreshState) {
            tabs {
                editor.childIds.forEach { containerId ->
                    val container = editor.model.getContainer(containerId)
                    tab(container.name) {
                        containerBuilder(editor.model, containerId)
                    }
                }
            }
        }
    }
}

fun ViewScope.textBuilder(editor: BlockEditor) {
    val blockState = editor.blockState.mutableTapOf({ it as TextBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val isEditingState = storeOf(textState.now.isEmpty())
    column {
        editorRow(editor, isEditingState)
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                textField(textState)
            } else {
                textBlock(textState.now.takeIf { it.isNotEmpty() } ?: "[Text content]")
            }
        }
    }
}

fun ViewScope.headingBuilder(editor: BlockEditor) {
    val blockState = editor.blockState.mutableTapOf({ it as HeadingBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val isEditingState = storeOf(textState.now.isEmpty())
    val levelState = blockState.mutableTapOf({ it.level }) { copy(level = it) }
    val hasFiligreeState = blockState.mutableTapOf({ it.hasFiligree }) { copy(hasFiligree = it) }
    column {
        editorRow(editor, isEditingState) {
            formColumn {
                formRow {
                    formSection("Level") {
                        dropMenu(levelState)
                        checkBox(hasFiligreeState, "filigree")
                    }
                }
            }
        }
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                textField(textState)
            } else {
                flowBlock(blockState) { block ->
                    box {
                        renderHeading(block)
                    }
                }
            }
        }
    }
}

fun ViewScope.richTextBuilder(editor: BlockEditor) {
    val blockState = editor.blockState.mutableTapOf({ it as RichTextBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val sizeState = blockState.mutableTapOf({ it.size ?: Size3.default }) {
        copy(size = it.takeIf { it != Size3.default })
    }
    val isEditingState = storeOf(textState.now.value.isEmpty())
    column {
        editorRow(editor, isEditingState) {
            formColumn {
                formSection("Size") {
                    dropMenu(sizeState)
                }
            }
        }
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                styledMarkdownEditor(textState)
            } else {
                flowBlock(blockState) { block ->
                    box {
                        renderRichText(block)
                    }
                }
            }
        }
    }
}

fun ViewScope.imageBuilder(editor: BlockEditor) {
    val blockState = editor.blockState.mutableTapOf({ it as ImageBlock }) { it }
    val imageState = blockState.mutableTapOf({ it.image }) { copy(image = it) }
    val shapeState = blockState.mutableTapOf({ it.shape ?: ImageShape.default }) {
        copy(shape = it.takeIf { it != ImageShape.default })
    }
    val fitState = blockState.mutableTapOf({ it.fit ?: ObjectFit.default }) {
        copy(fit = it.takeIf { it != ObjectFit.default })
    }
    val widthState = blockState.mutableTapOf({ it.width ?: 100 }) { copy(width = it.takeIf { it != 100 }) }
    column {
        editorRow(editor) {
            formColumn {
                row(modify(FlexItems1)) {
                    formSection("Shape") {
                        dropMenu(shapeState)
                    }
                    formSection("Fit") {
                        dropMenu(fitState)
                    }
                }
                formSection("Width") {
                    slider(widthState)
                }
            }
        }
        imageDrop(imageState) { _ ->
            flowBlock(blockState) { block ->
                box {
                    renderImage(block)
                }
            }
        }
    }
}

fun ViewScope.galleryBuilder(editor: BlockEditor) {
    val blockState = editor.blockState.mutableTapOf({ it as GalleryBlock }) { it }
    val newImageState = blockState.mutableTapOf<GalleryBlock, Image?>({ null }) {
        if (it == null) return@mutableTapOf this
        copy(images = images + it)
    }
    val shapeState = blockState.mutableTapOf({ it.shape ?: ImageShape.default }) {
        copy(shape = it.takeIf { it != ImageShape.default })
    }
    column {
        editorRow(editor) {
            formColumn {
                formSection("Shape") {
                    dropMenu(shapeState)
                }
            }
        }
        flowBlock(blockState) { block ->
            div(modify(LayoutStyle.Gallery)) {
                setStyle(Property.ColumnCount.to(block.columns))
                block.images.forEachIndexed { index, _ ->
                    val indexedImageState = blockState.mutableTapOf({ it.images.getOrNull(index) }) { indexedImage ->
                        copy(images = if (indexedImage == null) {
                            images - images[index]
                        } else images )
                    }
                    imageDrop(indexedImageState, modify(block.shape.toMod(), OverflowClip))
                }

                imageDrop(newImageState)
            }
        }
    }
}

fun ViewScope.columnBuilder(editor: BlockEditor) {
    // val blockState = editor.blockState.mutableTapOf({ it as ColumnBlock }) { it }
    val containerId = editor.childIds.first()
    val container = editor.model.getContainer(containerId)
    column {
        editorRow(editor)
        flowBlock(container.blockIdsField) { blockIds ->
            row(modify(BodyStyle.FormRow)) {
                blockIds.forEach { blockId ->
                    blockBuilder(editor.model, blockId)
                }
                lastEditorRow(container)
            }
        }
    }
}