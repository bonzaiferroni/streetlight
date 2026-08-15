package streetlight.web.model

import kampfire.model.Messenger
import koala.Image
import koala.model.tapOf
import koala.model.storeOf
import streetlight.model.data.DefaultLayout
import streetlight.model.data.ImageBlock
import streetlight.model.data.PageLayout
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import streetlight.model.data.TabsBlock
import streetlight.web.io.ApiClient
import kotlin.uuid.Uuid

class LayoutEditor(
    initialLayout: PageLayout,
    private val api: ApiClient,
) {
    private val state = storeOf(LayoutEditorState())

    private val blocks = mutableMapOf<BlockId, BlockEditor>()
    private val containers = mutableMapOf<ContainerId, ContainerEditor>()

    val movingBlockIdState = state.tapOf { it.movingBlockId }
    val removedBlockIdsState = state.tapOf { it.removedBlockIds }

    val mainContainerId = createContainer(initialLayout, 0)

    fun getBlock(blockId: BlockId) = blocks.getValue(blockId)
    fun getContainer(containerId: ContainerId) = containers.getValue(containerId)

    fun addBlock(block: LayoutBlock, containerId: ContainerId, index: Int) {
        val container = getContainer(containerId)
        val blockId = createBlock(block, container.depth)
        container.addBlock(blockId, index)
    }

    fun addBlockAbove(blockId: BlockId, block: LayoutBlock) {
        val container = getParent(blockId)
        val index = container.childIds.indexOf(blockId)
        addBlock(block, container.containerId, index)
    }

    fun getParentOrNull(blockId: BlockId) = containers.firstNotNullOfOrNull {
        if (it.value.childIds.contains(blockId)) it.value else null
    }

    fun getParent(blockId: BlockId) = getParentOrNull(blockId) ?: error("parent container not found")

    fun getParentOrNull(containerId: ContainerId) = blocks.firstNotNullOfOrNull {
        if (it.value.childIds.contains(containerId)) it.value else null
    }

    fun getParent(containerId: ContainerId) = getParentOrNull(containerId) ?: error("parent block not found")

    fun createContainer(blockId: BlockId, container: LayoutContainer): ContainerId {
        val parentContainer = getParent(blockId)
        return createContainer(container, parentContainer.depth + 1)
    }

    fun createContainer(container: LayoutContainer, depth: Int): ContainerId {
        val containerId = ContainerId(Uuid.random())
        val blockIds = container.blocks.map { block ->
            createBlock(block, depth)
        }
        containers[containerId] = ContainerEditor(containerId, container.name, blockIds, depth, this)
        return containerId
    }

    private fun createBlock(block: LayoutBlock, depth: Int): BlockId {
        val blockId = BlockId(Uuid.random())
        val containers = block.getContainers()
        val childIds = containers?.map { subContainer ->
            createContainer(subContainer, depth + 1)
        } ?: emptyList()
        blocks[blockId] = BlockEditor(blockId, block, childIds, this)
        return blockId
    }

    fun startMove(blockId: BlockId) {
        state.set { copy(movingBlockId = blockId) }
    }

    fun cancelMove() {
        state.set { copy(movingBlockId = null) }
    }

    fun removeFromContainer(blockId: BlockId) {
        val container = getParentOrNull(blockId) ?: return
        container.removeBlock(blockId)
    }

    fun finishMove(blockId: BlockId) {
        val movingBlockId = state.now.movingBlockId ?: error("moving blockId not found")
        removeFromContainer(movingBlockId)
        val container = getParent(blockId)
        val index = container.childIds.indexOf(blockId)
        container.addBlock(movingBlockId, index)
        state.set { copy(movingBlockId = null, removedBlockIds = removedBlockIds - movingBlockId) }
    }

    fun finishMoveToContainer(containerId: ContainerId) {
        val movingBlockId = state.now.movingBlockId ?: error("moving blockId not found")
        removeFromContainer(movingBlockId)
        val container = getContainer(containerId)
        val blockEditor = getBlock(movingBlockId)
        container.addBlock(blockEditor.blockId)
        state.set { copy(movingBlockId = null, removedBlockIds = removedBlockIds - movingBlockId) }
    }

    fun removeFromLayout(blockId: BlockId) {
        removeFromContainer(blockId)
        state.set { copy(removedBlockIds = removedBlockIds + blockId) }
    }

    suspend fun buildLayout(messenger: Messenger): PageLayout? {
        val blocks = buildContainer(mainContainerId, messenger).takeIf { it.isNotEmpty() } ?: return null
        return PageLayout(blocks).takeIf { it != DefaultLayout.location }
    }

    private suspend fun buildContainer(containerId: ContainerId, messenger: Messenger): List<LayoutBlock> {
        val container = getContainer(containerId)

        return container.childIds.mapNotNull { blockId ->
            buildBlock(blockId, messenger)
        }
    }

    private suspend fun buildBlock(blockId: BlockId, messenger: Messenger): LayoutBlock? {
        val blockEditor = getBlock(blockId)
        val block = processBlock(blockEditor.blockField.now, messenger)

        val containers = blockEditor.childIds.map { containerId ->
            val container = getContainer(containerId)
            ContainerDefinition(container.name, buildContainer(containerId, messenger))
        }
        return buildBlock(block, containers)
    }

    private suspend fun processBlock(block: LayoutBlock, messenger: Messenger) = when (block) {
        is ImageBlock -> {
            block.image?.let { image ->
                uploadImage(image.url, messenger, api)?.let { url ->
                    block.copy(image = image.copy(url = url))
                }
            } ?: block
        }
        else -> block
    }
}

data class LayoutEditorState(
    val movingBlockId: BlockId? = null,
    val removedBlockIds: List<BlockId> = emptyList(),
)

fun LayoutBlock.getContainers(): List<LayoutContainer>? = when (this) {
    is TabsBlock -> tabs
    else -> null
}

data class ContainerDefinition(
    val name: String,
    val blocks: List<LayoutBlock>
)

value class ContainerId(val value: Uuid)
value class BlockId(val value: Uuid)