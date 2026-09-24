package streetlight.web.model

import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import kotlin.time.Clock
import kotlin.time.Instant

/** One block of a [LayoutEditor], with the ids of the containers it holds. */
class BlockEditor(
    val blockId: BlockId,
    block: LayoutBlock,
    containerIds: List<ContainerId>,
    val model: LayoutEditor,
) {
    private val state = storeOf(BlockState(block, containerIds))
    val blockState = state.mutableTapOf({ it.block }) { copy(block = it) }
    val refreshState = state.tapOf { it.refreshAt }
    val childIds get() = state.now.childIds
    val parent get() = model.getParentOrNull(blockId)
    val parentId get() = parent?.containerId
    val index get() = parent?.childIds?.indexOf(blockId)
    val depth get() = parent?.depth
    val block get() = state.now.block
    val blockType get() = block.blockType
    val label get() = blockType.label

    /** Whether this block's container belongs to the block [otherId]. */
    fun isChildOf(otherId: BlockId) = model.getBlock(otherId).childIds.any { it == parentId }

    /** Whether this block directly follows [otherId] in the same container. */
    fun isNextSiblingOf(otherId: BlockId) = model.getParentOrNull(otherId)?.takeIf { it.containerId == parentId }?.let {
        it.childIds.indexOf(otherId) + 1 == index
    } ?: false

    /** Inserts [block] before this one in its container. */
    fun addBlockAbove(block: LayoutBlock) {
        model.addBlockAbove(blockId, block)
    }

    fun renameContainer(containerId: ContainerId, name: String) {
        val container = model.getContainer(containerId)
        container.rename(name)
        state.set { copy(refreshAt = Clock.System.now()) }
    }

    /** Adds [container] as a new child of this block. */
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

    /** Asks the views of this block to render again, through [BlockState.refreshAt]. */
    fun refresh() {
        state.set { copy(refreshAt = Clock.System.now()) }
    }
}

/** The state of a [BlockEditor]; a change of [refreshAt] alone asks for a new render. */
data class BlockState(
    val block: LayoutBlock,
    val childIds: List<ContainerId>,
    val refreshAt: Instant = Instant.DISTANT_PAST,
)