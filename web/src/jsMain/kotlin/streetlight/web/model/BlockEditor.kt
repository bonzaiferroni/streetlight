package streetlight.web.model

import koala.model.MutableTap
import koala.model.tapOf
import koala.model.mutableTapOf
import koala.model.storeOf
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import kotlin.time.Clock
import kotlin.time.Instant

class BlockEditor(
    val blockId: BlockId,
    block: LayoutBlock,
    containerIds: List<ContainerId>,
    val model: LayoutEditor,
) {
    private val state = storeOf(BlockState(block, containerIds))
    val blockField = state.mutableTapOf({ it.block }) { copy(block = it) }
    val refreshField = state.tapOf { it.refreshAt }
    val childIds get() = state.now.childIds
    val parent get() = model.getParentOrNull(blockId)
    val parentId get() = parent?.containerId
    val index get() = parent?.childIds?.indexOf(blockId)
    val depth get() = parent?.depth
    val block get() = state.now.block
    val blockType get() = block.blockType
    val label get() = blockType.label

    inline fun <reified T : LayoutBlock, V> mutableFieldOf(
        crossinline getter: (T) -> V,
        crossinline setter: T.(V) -> T
    ): MutableTap<V> = blockField.mutableTapOf(
        readValue = { getter(it as T) },
        writeValue = { value -> (this as T).setter(value) }
    )

    fun isChildOf(otherId: BlockId) = model.getBlock(otherId).childIds.any { it == parentId }

    fun isNextSiblingOf(otherId: BlockId) = model.getParentOrNull(otherId)?.takeIf { it.containerId == parentId }?.let {
        it.childIds.indexOf(otherId) + 1 == index
    } ?: false

    fun addBlockAbove(block: LayoutBlock) {
        model.addBlockAbove(blockId, block)
    }

    fun renameContainer(containerId: ContainerId, name: String) {
        val container = model.getContainer(containerId)
        container.rename(name)
        state.set { copy(refreshAt = Clock.System.now()) }
    }

    fun addContainer(container: LayoutContainer) {
        val key = model.createContainer(blockId, container)
        state.set {
            copy(
                childIds = this.childIds + key,
                refreshAt = Clock.System.now()
            )
        }
    }

    fun removeContainer(containerId: ContainerId) {
        state.set {
            copy(
                childIds = childIds.filter { it != containerId },
                refreshAt = Clock.System.now()
            )
        }
    }

    fun removeFromLayout() {
        model.removeFromLayout(blockId)
    }

    fun refresh() {
        state.set { copy(refreshAt = Clock.System.now()) }
    }
}

data class BlockState(
    val block: LayoutBlock,
    val childIds: List<ContainerId>,
    val refreshAt: Instant = Instant.DISTANT_PAST,
)