package streetlight.web.view

import koala.Image
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toggle
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.layout.renderHeading
import streetlight.web.layout.renderImage
import streetlight.web.layout.renderRichText
import streetlight.web.layout.toMod
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor
import koala.dom.component.*
import koala.dom.layout.*
import koala.dom.markdown.*
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.LayoutStyle
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.component.slider

/** The editor of the block [blockId], by the kind of block. */
context(model: LayoutEditor)
fun ViewScope.blockBuilder(blockId: BlockId) {
    val editor = model.getBlock(blockId)
    context(editor) {
        when (val block = editor.blockState.now) {
            is HeaderBlock -> headerBuilder()
            is EventsBlock, PostsBlock, MapBlock, CommentsBlock, SkillsBlock -> blockBuilder()
            is HeadingBlock -> headingBuilder()
            is ImageBlock -> imageBuilder()
            is GalleryBlock -> galleryBuilder()
            is RichTextBlock -> richTextBuilder()
            is TextBlock -> textBuilder()
            is TabsBlock -> tabsBuilder()
            is ColumnsBlock -> columnBuilder()
        }
    }
}

/** The editor row of a block above its [content]. */
context(editor: BlockEditor)
fun ViewScope.blockBuilder(
    content: FlowContent.() -> Unit
) = column {
    editorRow()
    content()
}

/** The editor of a block whose content is drawn from its page, shown as a placeholder. */
context(editor: BlockEditor)
fun ViewScope.blockBuilder() {
    val blockType = editor.block.blockType
    blockBuilder() {
        buildContentBlock(blockType)
    }
}

/** The editor of a header block: its layout, and whether it shows its cells and body. */
context(editor: BlockEditor)
fun ViewScope.headerBuilder() {
    val blockState = editor.blockState.mutableTapOf({ it as HeaderBlock }) { it }
    val headerTypeState = blockState.mutableTapOf({ it.headerType }) { copy(headerType = it) }
    val showCellsState = blockState.mutableTapOf({ it.showCells }) { copy(showCells = it) }
    val showBodyState = blockState.mutableTapOf({ it.showBody }) { copy(showBody = it) }
    column {
        editorRow() {
            formColumn {
                formSection("Header") {
                    dropMenu(headerTypeState)
                    checkBox(showCellsState, "cells")
                    checkBox(showBodyState, "body")
                }
            }
        }
        buildContentBlock(BlockType.Header)
    }
}

fun ViewScope.buildContentBlock(type: BlockType) {
    box(modify(Outline, BorderRadius1, ZenBg, Height(48))) {
        textBlock("${type.label} content", modify(TextUppercase, TextSmall, OpacityHigh, PlaceSelfCenter))
    }
}

context(editor: BlockEditor)
fun ViewScope.tabsBuilder() {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        editorRow() {
            flowBlock(editor.refreshState) {
                column {
                    editor.childIds.forEach { containerId ->
                        val tabName = editor.model.getContainer(containerId).name
                        val isEditingField = storeOf(false)
                        flowBlock(isEditingField, Height(5)) { isEditing ->
                            if (isEditing) {
                                val tabNameField = storeOf(tabName)
                                textField(tabNameField, onEnterSubmit = {
                                    editor.renameContainer(containerId, tabNameField.now)
                                })
                            } else {
                                row(modify(AlignItemsCenter, Height(5))) {
                                    textBlock(tabName, Flex1)
                                    button(SvgFile.Edit.filled, { isEditingField.toggle() })
                                    button(SvgFile.Minus.small, {
                                        editor.removeContainer(containerId)
                                    })
                                }
                            }
                        }
                    }
                    button(SvgFile.Plus.small, {
                        editor.addContainer(TabContent("new tab", emptyList()))
                    })
                }
            }
        }

        flowBlock(editor.refreshState) {
            lazyTabs {
                editor.childIds.forEach { containerId ->
                    val container = editor.model.getContainer(containerId)
                    tab(container.name) {
                        context(editor.model) {
                            containerBuilder(containerId)
                        }
                    }
                }
            }
        }
    }
}

context(editor: BlockEditor)
fun ViewScope.textBuilder() {
    val blockState = editor.blockState.mutableTapOf({ it as TextBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val isEditingState = storeOf(textState.now.isEmpty())
    column {
        editorRow(isEditingState)
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                textField(textState)
            } else {
                textBlock(textState.now.takeIf { it.isNotEmpty() } ?: "[Text content]")
            }
        }
    }
}

context(editor: BlockEditor)
fun ViewScope.headingBuilder() {
    val blockState = editor.blockState.mutableTapOf({ it as HeadingBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val isEditingState = storeOf(textState.now.isEmpty())
    val levelState = blockState.mutableTapOf({ it.level }) { copy(level = it) }
    val hasFiligreeState = blockState.mutableTapOf({ it.hasFiligree }) { copy(hasFiligree = it) }
    column {
        editorRow(isEditingState) {
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

context(editor: BlockEditor)
fun ViewScope.richTextBuilder() {
    val blockState = editor.blockState.mutableTapOf({ it as RichTextBlock }) { it }
    val textState = blockState.mutableTapOf({ it.text }) { copy(text = it) }
    val sizeState = blockState.mutableTapOf({ it.size ?: Size3.default }) {
        copy(size = it.takeIf { it != Size3.default })
    }
    val isEditingState = storeOf(textState.now.value.isEmpty())
    column {
        editorRow(isEditingState) {
            formColumn {
                formSection("Size") {
                    dropMenu(sizeState)
                }
            }
        }
        flowBlock(isEditingState) { isEditing ->
            if (isEditing) {
                markdownEditor(textState)
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

context(editor: BlockEditor)
fun ViewScope.imageBuilder() {
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
        editorRow() {
            formColumn {
                row(FlexItems1) {
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

context(editor: BlockEditor)
fun ViewScope.galleryBuilder() {
    val blockState = editor.blockState.mutableTapOf({ it as GalleryBlock }) { it }
    val newImageState = blockState.mutableTapOf<GalleryBlock, Image?>({ null }) {
        if (it == null) return@mutableTapOf this
        copy(images = images + it)
    }
    val shapeState = blockState.mutableTapOf({ it.shape ?: ImageShape.default }) {
        copy(shape = it.takeIf { it != ImageShape.default })
    }
    column {
        editorRow() {
            formColumn {
                formSection("Shape") {
                    dropMenu(shapeState)
                }
            }
        }
        flowBlock(blockState) { block ->
            div(LayoutStyle.Gallery) {
                setStyle(Css.ColumnCount.of(block.columns))
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

context(editor: BlockEditor)
fun ViewScope.columnBuilder() {
    // val blockState = editor.blockState.mutableTapOf({ it as ColumnBlock }) { it }
    val containerId = editor.childIds.first()
    val container = editor.model.getContainer(containerId)
    column {
        editorRow()
        flowBlock(container.blockIdsField) { blockIds ->
            row(BodyStyle.ColumnRow) {
                context(editor.model) {
                    blockIds.forEach { blockId ->
                        blockBuilder(blockId)
                    }
                }
                context(container) {
                    lastEditorRow()
                }
            }
        }
    }
}