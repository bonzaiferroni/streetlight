package streetlight.model.data

import kampfire.model.Labeled
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class ImageBlock(
    val image: Image?,
    val frame: ImageFrame? = null,
    val fit: ObjectFit? = null,
): LayoutBlock {
    override val blockType get() = BlockType.Image
}

enum class ImageFrame: Labeled {
    Circle, Ellipse, Pill, Chopped;
    override val label get() = name
}

enum class ObjectFit(label: String? = null): Labeled {
    Fill, Contain, Cover, ScaleDown("Scale Down");
    override val label = label ?: name
}