package streetlight.model.data

import kampfire.model.Labeled
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class ImageBlock(
    val image: Image?,
    val frame: ImageShape? = null,
    val fit: ObjectFit? = null,
    val width: Int? = null,
): LayoutBlock {
    override val blockType get() = BlockType.Image
}

enum class ImageShape: Labeled {
    Square, Rounded, Circle, Ellipse, Pill, Chopped;
    override val label get() = name

    companion object {
        val default = Rounded
    }
}

enum class ObjectFit(label: String? = null): Labeled {
    Fill, Stretch, Contain, Cover, ScaleDown("Scale Down");
    override val label = label ?: name

    companion object {
        val default = Fill
    }
}