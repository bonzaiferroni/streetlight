package streetlight.web.model

import koala.model.tapOf
import koala.model.storeOf
import streetlight.model.data.PageLayout
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import streetlight.model.data.TabsBlock
import kotlin.uuid.Uuid

class LayoutEditor(initialLayout: PageLayout) {
    private val state = storeOf(LayoutEditorState())

    private val blocks = mutableMapOf<Uuid, BlockEditor>()
    private val containers = mutableMapOf<Uuid, ContainerEditor>()

    val movingBlockField = state.tapOf { it.movingBlockId }

    val mainContainerId = createContainer(initialLayout, 0).id

    fun getBlock(blockId: Uuid) = blocks.getValue(blockId)
    fun getContainer(containerId: Uuid) = containers.getValue(containerId)

    fun addBlock(block: LayoutBlock, containerId: Uuid, index: Int) {
        val container = getContainer(containerId)
        val blockId = createBlock(block, container.depth)
        container.addBlock(blockId, index)
    }

    fun addBlockAbove(blockId: Uuid, block: LayoutBlock) {
        val container = getContainerWithBlock(blockId)
        val index = container.blockIds.indexOf(blockId)
        addBlock(block, container.containerId, index)
    }

    fun getContainerWithBlock(blockId: Uuid) = containers.firstNotNullOf {
        if (it.value.blockIds.contains(blockId)) it.value else null
    }

    fun createContainer(blockId: Uuid, container: LayoutContainer): ContainerKey {
        val parentContainer = getContainerWithBlock(blockId)
        return createContainer(container, parentContainer.depth + 1)
    }

    fun createContainer(container: LayoutContainer, depth: Int): ContainerKey {
        val containerId = Uuid.random()
        val blockIds = container.blocks.map { block ->
            createBlock(block, depth)
        }
        containers[containerId] = ContainerEditor(containerId, container.name, blockIds, depth, this)
        return ContainerKey(container.name, containerId)
    }

    private fun createBlock(block: LayoutBlock, depth: Int): Uuid {
        val blockId = Uuid.random()
        val containers = block.getContainers()
        val containerKeys = containers?.map { subContainer ->
            createContainer(subContainer, depth + 1)
        }
        blocks[blockId] = BlockEditor(blockId, block, containerKeys, this)
        return blockId
    }

    fun buildLayout(): PageLayout? {
        val blocks = buildContainer(mainContainerId).takeIf { it.isNotEmpty() } ?: return null
        return PageLayout(blocks)
    }

    fun startMove(blockId: Uuid) {
        state.set { copy(movingBlockId = blockId) }
    }

    fun cancelMove() {
        state.set { copy(movingBlockId = null) }
    }

    fun cutBlock(blockId: Uuid) {
        val container = getContainerWithBlock(blockId)
        container.removeBlock(blockId)
    }

    fun finishMove(blockId: Uuid) {
        val movingBlockId = state.now.movingBlockId ?: error("moving blockId not found")
        cutBlock(movingBlockId)
        val container = getContainerWithBlock(blockId)
        val index = container.blockIds.indexOf(blockId)
        container.addBlock(movingBlockId, index)
        state.set { copy(movingBlockId = null) }
    }

    fun finishMoveToContainer(containerId: Uuid) {
        val movingBlockId = state.now.movingBlockId ?: error("moving blockId not found")
        cutBlock(movingBlockId)
        val container = getContainer(containerId)
        val blockEditor = getBlock(movingBlockId)
        container.addBlock(blockEditor.blockId)
        state.set { copy(movingBlockId = null) }
    }

    private fun buildContainer(containerId: Uuid): List<LayoutBlock> {
        val container = getContainer(containerId)

        return container.blockIds.mapNotNull { blockId ->
            buildBlock(blockId)
        }
    }

    private fun buildBlock(blockId: Uuid): LayoutBlock? {
        val blockEditor = getBlock(blockId)
        val block = blockEditor.blockField.now
        val containers = blockEditor.containerKeys?.map { key ->
            ContainerDefinition(key.name, buildContainer(key.id))
        }
        return buildBlock(block, containers)
    }
}

data class LayoutEditorState(
    val movingBlockId: Uuid? = null,
)

fun LayoutBlock.getContainers(): List<LayoutContainer>? = when (this) {
    is TabsBlock -> tabs
    else -> null
}

data class ContainerKey(
    val name: String,
    val id: Uuid,
)

data class ContainerDefinition(
    val name: String,
    val blocks: List<LayoutBlock>
)